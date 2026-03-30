package model;

public class OrderItem {
    private String productId;
    private String productName;
    private double price;
    private int quantity;
    private int returnedQuantity;

    public OrderItem(String productId, String productName, double price, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.returnedQuantity = 0;
    }

    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public int getReturnedQuantity() { return returnedQuantity; }
    public int getActiveQuantity() { return quantity - returnedQuantity; }
    public double getSubtotal() { return price * getActiveQuantity(); }

    public boolean returnItems(int qty) {
        if (qty <= getActiveQuantity()) {
            returnedQuantity += qty;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        String returnInfo = returnedQuantity > 0 ? " [Returned: " + returnedQuantity + "]" : "";
        return String.format("  [%s] %s x%d @ ₹%.2f = ₹%.2f%s",
                productId, productName, quantity, price, price * quantity, returnInfo);
    }
}
