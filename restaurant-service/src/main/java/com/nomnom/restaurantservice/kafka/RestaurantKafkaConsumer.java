package com.nomnom.restaurantservice.kafka;

import com.nomnom.restaurantservice.event.OrderConfirmedEvent;
import com.nomnom.restaurantservice.event.OrderPlacedEvent;
import com.nomnom.restaurantservice.repository.MenuItemRepository;
import com.nomnom.restaurantservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// Kafka Consumer for Restaurant Service
// Listens to "order.placed" published by Order Service.
// Validates that all ordered items exist and are available in this restaurant,
// then publishes "order.confirmed" (CONFIRMED or REJECTED) back onto the bus.
@Component
@RequiredArgsConstructor
public class RestaurantKafkaConsumer {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantKafkaProducer restaurantKafkaProducer;

    @KafkaListener(
            topics = "order.placed",
            groupId = "restaurant-service"
    )
    public void consumeOrderPlaced(OrderPlacedEvent event) {

        System.out.println("Received OrderPlaced event: " + event);

        // Find the restaurant this order belongs to
        var restaurantOpt = restaurantRepository.findByRestaurantCode(event.getRestaurantCode());

        if (restaurantOpt.isEmpty()) {
            System.out.println("Restaurant not found for order: " + event.getOrderId()
                    + " — publishing REJECTED.");

            restaurantKafkaProducer.publishOrderConfirmed(new OrderConfirmedEvent(
                    event.getOrderId(),
                    event.getRestaurantCode(),
                    "REJECTED",
                    null,
                    event.getCustomerCode(),
                    LocalDateTime.now().toString()
            ));
            return;
        }

        var restaurant = restaurantOpt.get();

        // Validate that every ordered item is available in this restaurant's menu
        boolean allItemsAvailable = true;

        if (event.getItems() != null) {
            for (OrderPlacedEvent.OrderedLineItem item : event.getItems()) {

                var menuItem = menuItemRepository.findByMenuItemCode(item.getMenuItemCode());

                if (menuItem.isEmpty() || !menuItem.get().isAvailable()) {
                    System.out.println("Item not available: " + item.getMenuItemCode()
                            + " — order cannot be confirmed.");
                    allItemsAvailable = false;
                    break;
                }
            }
        }

        if (!allItemsAvailable) {
            System.out.println("Order " + event.getOrderId() + " was NOT confirmed (item unavailable).");

            restaurantKafkaProducer.publishOrderConfirmed(new OrderConfirmedEvent(
                    event.getOrderId(),
                    restaurant.getRestaurantCode(),
                    "REJECTED",
                    restaurant.getName(),
                    event.getCustomerCode(),
                    LocalDateTime.now().toString()
            ));
            return;
        }

        // All items are available — confirm the order
        OrderConfirmedEvent confirmedEvent = new OrderConfirmedEvent(
                event.getOrderId(),
                restaurant.getRestaurantCode(),
                "CONFIRMED",
                restaurant.getName(),
                event.getCustomerCode(),
                LocalDateTime.now().toString()
        );

        restaurantKafkaProducer.publishOrderConfirmed(confirmedEvent);
        System.out.println("Published OrderConfirmed event for order: " + event.getOrderId());
    }
}
