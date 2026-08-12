package com.nomnom.orderservice.service;

import com.nomnom.orderservice.entity.Money;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PricingServiceImpl implements PricingService {

    // Business rule (assumption): flat delivery fee, waived once the
    // subtotal reaches the free-delivery threshold.
    private static final Money FLAT_DELIVERY_FEE = Money.of(BigDecimal.valueOf(5.00));
    private static final Money FREE_DELIVERY_THRESHOLD = Money.of(BigDecimal.valueOf(100.00));

    @Override
    public Money calculateDeliveryFee(Money subtotal) {

        if (subtotal.isGreaterThanOrEqualTo(FREE_DELIVERY_THRESHOLD)) {
            return Money.zero();
        }

        return FLAT_DELIVERY_FEE;
    }
}
