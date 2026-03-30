package model;

import model.enums.PaymentStatus;

public class Payment {
    private String paymentId;
    private String orderId;
    private String userId;
    private double amount;
    private PaymentStatus status;
    private long timestamp;

    public Payment(String paymentId, String orderId, String userId, double amount) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
        this.timestamp = System.currentTimeMillis();
    }

    public String getPaymentId() { return paymentId; }
    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public double getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public long getTimestamp() { return timestamp; }

    public void setStatus(PaymentStatus status) {
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return String.format("Payment[%s] Order=%s Amount=₹%.2f Status=%s",
                paymentId, orderId, amount, status);
    }
}
