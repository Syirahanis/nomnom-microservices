package com.nomnom.orderservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DDD building block: Aggregate Root of the Order bounded context.
 * <p>
 * Order is the only entry point for changing anything inside the aggregate:
 * {@link OrderItem}s are added only through {@link #addItem}, pricing is
 * only applied through {@link #applyDeliveryFee}, and status only moves
 * forward through {@link #confirm()}, {@link #markDelivered()} and
 * {@link #cancel()} - each of which enforces the valid state transitions
 * for an order. Callers (the application service layer) are never allowed
 * to reach into an OrderItem or flip the status field directly.
 * <p>
 * customerCode and restaurantCode are plain String references, not JPA
 * relationships - Order Service owns its own database (database-per-service)
 * and does not share tables with Customer Service or the future
 * Restaurant & Catalog Service, so those bounded contexts are only ever
 * linked by these business identifiers, never by a foreign key.
 */
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // required by Hibernate
@ToString(exclude = "items")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderCode;

    @Column(nullable = false)
    private String customerCode;

    @Column(nullable = false)
    private String restaurantCode;

    @Column(nullable = false)
    private String deliveryAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "subtotal_amount"))
    @AttributeOverride(name = "currency", column = @Column(name = "subtotal_currency"))
    private Money subtotal = Money.zero();

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "delivery_fee_amount"))
    @AttributeOverride(name = "currency", column = @Column(name = "delivery_fee_currency"))
    private Money deliveryFee = Money.zero();

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "total_amount_amount"))
    @AttributeOverride(name = "currency", column = @Column(name = "total_amount_currency"))
    private Money totalAmount = Money.zero();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    private LocalDateTime placedAt;

    private LocalDateTime confirmedAt;

    private LocalDateTime deliveredAt;

    private Order(String orderCode, String customerCode, String restaurantCode, String deliveryAddress) {
        this.orderCode = orderCode;
        this.customerCode = customerCode;
        this.restaurantCode = restaurantCode;
        this.deliveryAddress = deliveryAddress;
        this.status = OrderStatus.PLACED;
        this.placedAt = LocalDateTime.now();
    }

    /**
     * Factory method - the only way to create a new Order. Starts out with
     * no items and PLACED status; call {@link #addItem} for each line item
     * and {@link #applyDeliveryFee} once pricing has been calculated.
     */
    public static Order place(String customerCode, String restaurantCode, String deliveryAddress, String orderCode) {
        return new Order(orderCode, customerCode, restaurantCode, deliveryAddress);
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Adds a line item to this order and immediately recalculates the
     * subtotal, keeping the aggregate's pricing invariant consistent at
     * all times.
     */
    public void addItem(String menuItemCode, String itemName, int quantity, Money unitPrice) {
        if (this.status != OrderStatus.PLACED) {
            throw new IllegalStateException("Cannot add items to an order that is already " + this.status);
        }

        OrderItem item = new OrderItem(menuItemCode, itemName, quantity, unitPrice);
        item.setOrder(this);
        this.items.add(item);

        recalculateSubtotal();
    }

    /**
     * Applies the delivery fee produced by the PricingService domain
     * service and recomputes the order total. Kept separate from
     * {@link #addItem} because delivery fee is a pricing policy decision,
     * not an item-level concern.
     */
    public void applyDeliveryFee(Money deliveryFee) {
        this.deliveryFee = deliveryFee;
        this.totalAmount = this.subtotal.add(this.deliveryFee);
    }

    private void recalculateSubtotal() {
        Money sum = Money.zero();
        for (OrderItem item : items) {
            sum = sum.add(item.getLineTotal());
        }
        this.subtotal = sum;
        this.totalAmount = this.subtotal.add(this.deliveryFee);
    }

    /** Triggered when this service consumes an OrderConfirmed event. */
    public void confirm() {
        if (this.status != OrderStatus.PLACED) {
            throw new IllegalStateException("Only a PLACED order can be confirmed (current status: " + this.status + ")");
        }
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    /** Triggered when this service consumes an OrderDelivered event. */
    public void markDelivered() {
        if (this.status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Only a CONFIRMED order can be marked delivered (current status: " + this.status + ")");
        }
        this.status = OrderStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    /** A customer may only cancel before the restaurant has confirmed it. */
    public void cancel() {
        if (this.status != OrderStatus.PLACED) {
            throw new IllegalStateException("Only a PLACED order can be cancelled (current status: " + this.status + ")");
        }
        this.status = OrderStatus.CANCELLED;
    }
}
