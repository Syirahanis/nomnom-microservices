package com.nomnom.orderservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * DDD Value Object.
 * <p>
 * Money has no identity of its own - two Money instances with the same
 * amount and currency are considered equal (see {@code @EqualsAndHashCode}).
 * There are no public setters: every operation below returns a brand new
 * Money instance instead of mutating this one, so once created a Money is
 * effectively immutable from the outside. All monetary values in NomNom are
 * assumed to be MYR (see PID assumptions - no real payment gateway is
 * integrated).
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED) // required by Hibernate
public class Money {

    private static final String DEFAULT_CURRENCY = "MYR";

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency;

    private Money(BigDecimal amount, String currency) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency;
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount, DEFAULT_CURRENCY);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO, DEFAULT_CURRENCY);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(int quantity) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)), this.currency);
    }

    public boolean isGreaterThanOrEqualTo(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount) >= 0;
    }

    private void requireSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Cannot operate on Money with different currencies: "
                            + this.currency + " vs " + other.currency);
        }
    }

    @Override
    public String toString() {
        return currency + " " + amount.toPlainString();
    }
}
