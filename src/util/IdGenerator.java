package util;

import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {
    private static final AtomicInteger productCounter = new AtomicInteger(1);
    private static final AtomicInteger orderCounter = new AtomicInteger(100);
    private static final AtomicInteger paymentCounter = new AtomicInteger(1);
    private static final AtomicInteger userCounter = new AtomicInteger(1);

    public static String generateProductId() {
        return "PROD_" + productCounter.getAndIncrement();
    }

    public static String generateOrderId() {
        return "ORDER_" + orderCounter.getAndIncrement();
    }

    public static String generatePaymentId() {
        return "PAY_" + paymentCounter.getAndIncrement();
    }

    public static String generateUserId() {
        return "USER_" + userCounter.getAndIncrement();
    }
}
