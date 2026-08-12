package com.nomnom.deliveryservice.service;

import com.nomnom.deliveryservice.dto.DeliveryResponse;
import com.nomnom.deliveryservice.dto.DeliveryStatusResponse;
import com.nomnom.deliveryservice.dto.UpdateDeliveryStatusRequest;
import com.nomnom.deliveryservice.event.OrderConfirmedEvent;

import java.util.List;

public interface DeliveryService {

    void handleOrderConfirmed(OrderConfirmedEvent event);

    DeliveryResponse getDeliveryByCode(String deliveryCode);

    DeliveryResponse getDeliveryByOrderId(String orderId);

    List<DeliveryResponse> getAllDeliveries();

    DeliveryStatusResponse getDeliveryStatus(String deliveryCode);

    DeliveryResponse updateDeliveryStatus(String deliveryCode, UpdateDeliveryStatusRequest request);

}
