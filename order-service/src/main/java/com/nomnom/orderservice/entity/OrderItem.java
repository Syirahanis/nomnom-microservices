package com.nomnom.orderservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DDD building block: child Entity within the Order aggregate.
 * <p>
 * An OrderItem has its own identity ({@code id}) so it is an Entity rather
 * than a Value Object, but per DDD aggregate rules it has no repository or
 * controller of its own - it is only created, read and modified through the
 * {@link Order} aggregate root. Line totals are recalculated by
 * {@link Order#recalculateTotals()} rather than exposed via a public setter,
 * to keep pricing invariants inside the aggregate.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter(AccessLevel.PACKAGE)
@NoArgsConstructor
@ToString(exclude = "order")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private String menuItemCode;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private int quantity;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "unit_price_amount"))
    @AttributeOverride(name = "currency", column = @Column(name = "unit_price_currency"))
    private Money unitPrice;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "line_total_amount"))
    @AttributeOverride(name = "currency", column = @Column(name = "line_total_currency"))
    private Money lineTotal;

    OrderItem(String menuItemCode, String itemName, int quantity, Money unitPrice) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Item quantity must be at least 1");
        }
        this.menuItemCode = menuItemCode;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = unitPrice.multiply(quantity);
    }
}
