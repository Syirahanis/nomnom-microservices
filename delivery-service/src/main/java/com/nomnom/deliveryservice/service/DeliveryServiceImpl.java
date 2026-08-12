package com.nomnom.deliveryservice.service;

import com.nomnom.deliveryservice.dto.DeliveryResponse;
import com.nomnom.deliveryservice.dto.DeliveryStatusResponse;
import com.nomnom.deliveryservice.dto.UpdateDeliveryStatusRequest;
import com.nomnom.deliveryservice.entity.Delivery;
import com.nomnom.deliveryservice.entity.DeliveryStatus;
import com.nomnom.deliveryservice.entity.Rider;
import com.nomnom.deliveryservice.event.DeliveryAssignedEvent;
import com.nomnom.deliveryservice.event.OrderConfirmedEvent;
import com.nomnom.deliveryservice.event.OrderDeliveredEvent;
import com.nomnom.deliveryservice.exception.InvalidDeliveryStateException;
import com.nomnom.deliveryservice.exception.ResourceNotFoundException;
import com.nomnom.deliveryservice.kafka.DeliveryKafkaProducer;
import com.nomnom.deliveryservice.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final RiderAssignmentService riderAssignmentService;
    private final DeliveryKafkaProducer deliveryKafkaProducer;

    @Override
    @Transactional
    public void handleOrderConfirmed(OrderConfirmedEvent event) {

        // Only CONFIRMED orders trigger rider assignment; REJECTED orders
        // are ignored (the order was not accepted by the restaurant).
        if (!"CONFIRMED".equalsIgnoreCase(event.getStatus())) {
            System.out.println(
                    "Ignoring OrderConfirmed for " + event.getOrderId()
                            + " - status is " + event.getStatus());
            return;
        }

        // if a delivery already exists for this order,
        // do not assign a second rider.
        if (deliveryRepository.findByOrderId(event.getOrderId()).isPresent()) {
            System.out.println(
                    "Ignoring OrderConfirmed for " + event.getOrderId()
                            + " - a delivery already exists.");
            return;
        }

        // Assign available rider
        Rider rider = riderAssignmentService.assignAvailableRider();

        // save delivery to local .db
        Delivery delivery = Delivery.assign(
                generateDeliveryCode(),
                event.getOrderId(),
                rider.getRiderCode(),
                resolveDeliveryAddress(event));

        Delivery saved = deliveryRepository.save(delivery);

        // Publish DeliveryAssigned
        publishDeliveryAssigned(saved);

        System.out.println(
                "Assigned rider " + rider.getRiderCode()
                        + " to order " + event.getOrderId()
                        + " (delivery " + saved.getDeliveryCode() + ")");
    }

    private String resolveDeliveryAddress(OrderConfirmedEvent event) {
        return "Address on file for order " + event.getOrderId();
    }

    private String generateDeliveryCode() {
        return "DEL" + System.currentTimeMillis();
    }

    private void publishDeliveryAssigned(Delivery delivery) {

        DeliveryAssignedEvent event = new DeliveryAssignedEvent(
                delivery.getDeliveryCode(),
                delivery.getOrderId(),
                delivery.getRiderId(),
                delivery.getDeliveryAddress(),
                delivery.getAssignedAt().toString());

        deliveryKafkaProducer.publishDeliveryAssigned(event);
    }

    @Override
    public DeliveryResponse getDeliveryByCode(String deliveryCode) {
        return toResponse(findDeliveryOrThrow(deliveryCode));
    }

    @Override
    public DeliveryResponse getDeliveryByOrderId(String orderId) {
        return toResponse(deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Delivery not found for order: " + orderId)));
    }

    @Override
    public List<DeliveryResponse> getAllDeliveries() {
        return deliveryRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DeliveryStatusResponse getDeliveryStatus(String deliveryCode) {

        Delivery delivery = findDeliveryOrThrow(deliveryCode);

        DeliveryStatusResponse response = new DeliveryStatusResponse();

        response.setDeliveryCode(delivery.getDeliveryCode());
        response.setOrderId(delivery.getOrderId());
        response.setRiderId(delivery.getRiderId());
        response.setStatus(delivery.getStatus());
        response.setAssignedAt(delivery.getAssignedAt());
        response.setDeliveredAt(delivery.getDeliveredAt());

        return response;
    }

    @Override
    @Transactional
    public DeliveryResponse updateDeliveryStatus(String deliveryCode, UpdateDeliveryStatusRequest request) {

        Delivery delivery = findDeliveryOrThrow(deliveryCode);

        DeliveryStatus target = request.getStatus();

        try {
            switch (target) {
                case IN_TRANSIT -> delivery.startTransit();
                case DELIVERED -> {
                    delivery.complete();
                    // Rider is free again once the delivery is done
                    riderAssignmentService.releaseRider(delivery.getRiderId());
                    publishOrderDelivered(delivery);
                }
                case ASSIGNED -> throw new InvalidDeliveryStateException(
                        "Cannot move a delivery back to ASSIGNED.");
                default -> throw new InvalidDeliveryStateException(
                        "Unsupported target status: " + target);
            }
        } catch (IllegalStateException ex) {
            throw new InvalidDeliveryStateException(ex.getMessage());
        }

        return toResponse(deliveryRepository.save(delivery));
    }

    private void publishOrderDelivered(Delivery delivery) {

        OrderDeliveredEvent event = new OrderDeliveredEvent(
                delivery.getOrderId(),
                null,
                "DELIVERED");

        deliveryKafkaProducer.publishOrderDelivered(event);

        System.out.println(
                "Published OrderDelivered for order " + delivery.getOrderId());
    }

    private Delivery findDeliveryOrThrow(String deliveryCode) {
        return deliveryRepository.findByDeliveryCode(deliveryCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Delivery not found with code: " + deliveryCode));
    }

    private DeliveryResponse toResponse(Delivery delivery) {

        DeliveryResponse response = new DeliveryResponse();

        response.setId(delivery.getId());
        response.setDeliveryCode(delivery.getDeliveryCode());
        response.setOrderId(delivery.getOrderId());
        response.setRiderId(delivery.getRiderId());
        response.setStatus(delivery.getStatus());
        response.setDeliveryAddress(delivery.getDeliveryAddress());
        response.setAssignedAt(delivery.getAssignedAt());
        response.setInTransitAt(delivery.getInTransitAt());
        response.setDeliveredAt(delivery.getDeliveredAt());

        return response;
    }
}
