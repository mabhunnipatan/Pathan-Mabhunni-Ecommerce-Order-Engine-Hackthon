package model;

import model.enums.OrderStatus;
import java.util.List;
import java.util.ArrayList;

public class Order {
    private String orderId;
    private String userId;
    private List<OrderItem> items;
    private double subtotal;
    private double discountAmount;
    private double total;
    private OrderStatus status;
    private String couponApplied;
    private long createdAt;
    private long updatedAt;
    private String idempotencyKey;

    public Order(String orderId, String userId, List<OrderItem> items,
                 double subtotal, double discountAmount, String couponApplied) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = new ArrayList<>(items);
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.total = Math.max(0, subtotal - discountAmount);
        this.status = OrderStatus.CREATED;
        this.couponApplied = couponApplied;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = createdAt;
    }

    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public List<OrderItem> getItems() { return items; }
    public double getSubtotal() { return subtotal; }
    public double getDiscountAmount() { return discountAmount; }
    public double getTotal() { return total; }
    public OrderStatus getStatus() { return status; }
    public String getCouponApplied() { return couponApplied; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public String getIdempotencyKey() { return idempotencyKey; }

    public void setStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public void setIdempotencyKey(String key) { this.idempotencyKey = key; }

    public void recalculateTotal() {
        this.subtotal = items.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
        double activeTotal = items.stream().mapToDouble(OrderItem::getSubtotal).sum();
        this.total = Math.max(0, activeTotal - discountAmount);
        this.updatedAt = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Order ID : %s%n", orderId));
        sb.append(String.format("User     : %s%n", userId));
        sb.append(String.format("Status   : %s%n", status));
        sb.append(String.format("Created  : %s%n", new java.util.Date(createdAt)));
        sb.append(String.format("Items:%n"));
        items.forEach(i -> sb.append(i).append("\n"));
        if (couponApplied != null) sb.append(String.format("Coupon   : %s (Discount: ₹%.2f)%n", couponApplied, discountAmount));
        sb.append(String.format("Subtotal : ₹%.2f%n", subtotal));
        sb.append(String.format("Total    : ₹%.2f%n", total));
        return sb.toString();
    }
}
