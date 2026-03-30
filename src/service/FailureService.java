package service;

import java.util.Random;

public class FailureService {
    private final Random random = new Random();
    private boolean globalFailureMode = false;
    private boolean paymentFailure = false;
    private boolean orderCreationFailure = false;
    private boolean inventoryFailure = false;

    public void setGlobalFailureMode(boolean enabled) {
        this.globalFailureMode = enabled;
        this.paymentFailure = enabled;
        this.orderCreationFailure = enabled;
        this.inventoryFailure = enabled;
    }

    public void setPaymentFailure(boolean enabled) { this.paymentFailure = enabled; }
    public void setOrderCreationFailure(boolean enabled) { this.orderCreationFailure = enabled; }
    public void setInventoryFailure(boolean enabled) { this.inventoryFailure = enabled; }

    public boolean isGlobalFailureMode() { return globalFailureMode; }
    public boolean shouldFailPayment() { return paymentFailure && random.nextBoolean(); }
    public boolean shouldFailOrderCreation() { return orderCreationFailure && random.nextBoolean(); }
    public boolean shouldFailInventory() { return inventoryFailure && random.nextBoolean(); }

    public void printStatus() {
        System.out.println("\n  ⚡ Failure Injection Status:");
        System.out.println("     Payment Failure      : " + (paymentFailure ? "ON" : "OFF"));
        System.out.println("     Order Creation Failure: " + (orderCreationFailure ? "ON" : "OFF"));
        System.out.println("     Inventory Failure     : " + (inventoryFailure ? "ON" : "OFF"));
    }
}
