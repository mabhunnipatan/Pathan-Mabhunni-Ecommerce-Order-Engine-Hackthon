package util;

public class Constants {
    public static final int LOW_STOCK_THRESHOLD = 5;
    public static final double HIGH_VALUE_ORDER_THRESHOLD = 5000.0;
    public static final int FRAUD_ORDER_COUNT = 3;
    public static final long FRAUD_WINDOW_MS = 60_000L; // 1 minute
    public static final long RESERVATION_EXPIRY_MS = 300_000L; // 5 minutes
    public static final double PAYMENT_FAILURE_RATE = 0.3; // 30% chance

    // Discount rules
    public static final double BULK_DISCOUNT_THRESHOLD = 1000.0;
    public static final double BULK_DISCOUNT_RATE = 0.10;
    public static final int QTY_DISCOUNT_MIN = 3;
    public static final double QTY_DISCOUNT_RATE = 0.05;

    // Coupons
    public static final String COUPON_SAVE10 = "SAVE10";
    public static final String COUPON_FLAT200 = "FLAT200";
}
