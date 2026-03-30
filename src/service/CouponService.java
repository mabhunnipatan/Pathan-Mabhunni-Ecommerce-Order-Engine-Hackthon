package service;

import model.Cart;
import model.CartItem;
import repository.CartRepository;
import util.Constants;

public class CouponService {
    private final CartRepository cartRepository;
    private final LoggingService loggingService;

    public CouponService(CartRepository cartRepository, LoggingService loggingService) {
        this.cartRepository = cartRepository;
        this.loggingService = loggingService;
    }

    public boolean applyCoupon(String userId, String couponCode) {
        Cart cart = cartRepository.getOrCreate(userId);
        if (cart.isEmpty()) {
            System.out.println("  ❌ Cart is empty.");
            return false;
        }
        if (cart.getAppliedCoupon() != null) {
            System.out.println("  ❌ A coupon is already applied: " + cart.getAppliedCoupon());
            return false;
        }

        double subtotal = cart.getSubtotal();
        double discount = calculateAutoDiscount(cart);
        double couponDiscount = getCouponDiscount(couponCode, subtotal);

        if (couponDiscount < 0) {
            System.out.println("  ❌ Invalid coupon code: " + couponCode);
            return false;
        }

        double totalDiscount = discount + couponDiscount;
        cart.setAppliedCoupon(couponCode);
        cart.setDiscountAmount(totalDiscount);

        loggingService.logAction(userId, "applied coupon", couponCode + " discount=₹" + totalDiscount);
        System.out.printf("  ✅ Coupon '%s' applied! Auto-discount: ₹%.2f + Coupon: ₹%.2f = Total discount: ₹%.2f%n",
                couponCode, discount, couponDiscount, totalDiscount);
        System.out.printf("  💰 New Total: ₹%.2f%n", cart.getTotal());
        return true;
    }

    public double calculateAutoDiscount(Cart cart) {
        double discount = 0.0;
        double subtotal = cart.getSubtotal();

        // Rule 1: Total > ₹1000 → 10% discount
        if (subtotal > Constants.BULK_DISCOUNT_THRESHOLD) {
            discount += subtotal * Constants.BULK_DISCOUNT_RATE;
        }

        // Rule 2: Quantity > 3 for same product → extra 5%
        for (CartItem item : cart.getItemList()) {
            if (item.getQuantity() > Constants.QTY_DISCOUNT_MIN) {
                discount += item.getSubtotal() * Constants.QTY_DISCOUNT_RATE;
            }
        }
        return discount;
    }

    private double getCouponDiscount(String code, double subtotal) {
        return switch (code.toUpperCase()) {
            case Constants.COUPON_SAVE10 -> subtotal * 0.10;
            case Constants.COUPON_FLAT200 -> 200.0;
            default -> -1;
        };
    }

    public double applyAutoDiscountsOnly(Cart cart) {
        if (cart.getAppliedCoupon() != null) return cart.getDiscountAmount();
        double discount = calculateAutoDiscount(cart);
        cart.setDiscountAmount(discount);
        return discount;
    }
}
