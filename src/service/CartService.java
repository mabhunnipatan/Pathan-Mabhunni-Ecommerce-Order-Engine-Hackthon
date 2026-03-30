package service;

import model.Cart;
import model.CartItem;
import model.Product;
import repository.CartRepository;
import repository.ProductRepository;

public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final LoggingService loggingService;

    public CartService(CartRepository cartRepository, ProductRepository productRepository,
                       InventoryService inventoryService, LoggingService loggingService) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
        this.loggingService = loggingService;
    }

    public boolean addToCart(String userId, String productId, int qty) {
        Product product = productRepository.findById(productId);
        if (product == null) {
            System.out.println("  ❌ Product not found: " + productId);
            return false;
        }
        if (qty <= 0) {
            System.out.println("  ❌ Quantity must be positive.");
            return false;
        }
        if (product.getAvailableStock() < qty) {
            System.out.println("  ❌ Not enough stock. Available: " + product.getAvailableStock());
            return false;
        }
        if (product.getAvailableStock() == 0) {
            System.out.println("  ❌ Product is out of stock.");
            return false;
        }

        boolean reserved = inventoryService.reserveStock(productId, userId, qty);
        if (!reserved) return false;

        Cart cart = cartRepository.getOrCreate(userId);
        CartItem item = new CartItem(productId, product.getName(), product.getPrice(), qty);
        cart.addItem(item);

        loggingService.logAction(userId, "added", productId + " qty=" + qty);
        System.out.println("  ✅ Added to cart: " + product.getName() + " x" + qty);
        return true;
    }

    public boolean removeFromCart(String userId, String productId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null || !cart.getItems().containsKey(productId)) {
            System.out.println("  ❌ Item not found in cart.");
            return false;
        }
        CartItem item = cart.getItems().get(productId);
        inventoryService.releaseReservation(productId, userId, item.getQuantity());
        cart.removeItem(productId);

        loggingService.logAction(userId, "removed", productId + " from cart");
        System.out.println("  ✅ Removed from cart: " + productId);
        return true;
    }

    public void viewCart(String userId) {
        Cart cart = cartRepository.getOrCreate(userId);
        System.out.println("\n  ┌─────────────────────────────────────────────────────────────┐");
        System.out.printf("  │  Cart for %-49s│%n", userId);
        System.out.println("  └─────────────────────────────────────────────────────────────┘");
        if (cart.isEmpty()) {
            System.out.println("  🛒 Cart is empty.");
            return;
        }
        cart.getItemList().forEach(System.out::println);
        System.out.printf("  Subtotal : ₹%.2f%n", cart.getSubtotal());
        if (cart.getAppliedCoupon() != null) {
            System.out.printf("  Coupon   : %s (Discount: ₹%.2f)%n", cart.getAppliedCoupon(), cart.getDiscountAmount());
        }
        System.out.printf("  Total    : ₹%.2f%n", cart.getTotal());
    }

    public Cart getCart(String userId) {
        return cartRepository.getOrCreate(userId);
    }

    public void clearCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart != null) cart.clear();
    }
}
