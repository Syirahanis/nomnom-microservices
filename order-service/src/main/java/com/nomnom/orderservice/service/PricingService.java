package com.nomnom.orderservice.service;

import com.nomnom.orderservice.entity.Money;

/**
 * DDD building block: Domain Service.
 * <p>
 * Pricing policy (how the delivery fee is decided) is a piece of domain
 * logic that does not naturally belong to {@code Order} or
 * {@code OrderItem} - it is a stateless calculation over the aggregate's
 * current subtotal, and the rule itself (flat fee, free-delivery
 * threshold, etc.) is a business policy the team may want to change
 * independently of the entity model.
 */
public interface PricingService {

    Money calculateDeliveryFee(Money subtotal);

}
