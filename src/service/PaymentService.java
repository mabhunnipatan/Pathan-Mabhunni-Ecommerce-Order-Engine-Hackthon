package service;

import model.Payment;
import model.enums.PaymentStatus;
import util.Constants;
import util.IdGenerator;

import java.util.Random;

public class PaymentService {
    private final LoggingService loggingService;
    private final Random random = new Random();
    private boolean failureMode = false;

    public PaymentService(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    public void setFailureMode(boolean failureMode) {
        this.failureMode = failureMode;
        System.out.println("  ⚡ Failure mode: " + (failureMode ? "ENABLED" : "DISABLED"));
    }

    public boolean isFailureMode() { return failureMode; }

    public Payment processPayment(String orderId, String userId, double amount) {
        Payment payment = new Payment(IdGenerator.generatePaymentId(), orderId, userId, amount);
        loggingService.log("Payment initiated for " + orderId + " amount=₹" + amount);

        boolean success = simulatePayment();
        if (success) {
            payment.setStatus(PaymentStatus.SUCCESS);
            loggingService.log("PAYMENT_SUCCESS " + payment.getPaymentId() + " order=" + orderId);
            System.out.println("  ✅ Payment successful: " + payment.getPaymentId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            loggingService.log("PAYMENT_FAILED " + payment.getPaymentId() + " order=" + orderId);
            System.out.println("  ❌ Payment failed: " + payment.getPaymentId());
        }
        return payment;
    }

    private boolean simulatePayment() {
        if (failureMode) return false;
        return random.nextDouble() > Constants.PAYMENT_FAILURE_RATE;
    }

    public Payment refund(String orderId, String userId, double amount) {
        Payment refund = new Payment(IdGenerator.generatePaymentId(), orderId, userId, amount);
        refund.setStatus(PaymentStatus.REFUNDED);
        loggingService.log("REFUND issued for " + orderId + " amount=₹" + amount);
        System.out.printf("  ✅ Refund of ₹%.2f issued for order %s%n", amount, orderId);
        return refund;
    }
}
