package model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collection;

public class Cart {
    private String userId;
    private Map<String, CartItem> items;
    private String appliedCoupon;
    private double discountAmount;

    public Cart(String userId) {
        this.userId = userId;
        this.items = new LinkedHashMap<>();
        this.appliedCoupon = null;
        this.discountAmount = 0.0;
    }

    public String getUserId() { return userId; }
    public Map<String, CartItem> getItems() { return items; }
    public Collection<CartItem> getItemList() { return items.values(); }
    public String getAppliedCoupon() { return appliedCoupon; }
    public double getDiscountAmount() { return discountAmount; }

    public void setAppliedCoupon(String coupon) { this.appliedCoupon = coupon; }
    public void setDiscountAmount(double amount) { this.discountAmount = amount; }

    public boolean isEmpty() { return items.isEmpty(); }

    public void addItem(CartItem item) {
        if (items.containsKey(item.getProductId())) {
            CartItem existing = items.get(item.getProductId());
            existing.setQuantity(existing.getQuantity() + item.getQuantity());
        } else {
            items.put(item.getProductId(), item);
        }
    }

    public boolean removeItem(String productId) {
        return items.remove(productId) != null;
    }

    public double getSubtotal() {
        return items.values().stream().mapToDouble(CartItem::getSubtotal).sum();
    }

    public double getTotal() {
        return Math.max(0, getSubtotal() - discountAmount);
    }

    public void clear() {
        items.clear();
        appliedCoupon = null;
        discountAmount = 0.0;
    }

    public int getTotalItems() {
        return items.values().stream().mapToInt(CartItem::getQuantity).sum();
    }
}
