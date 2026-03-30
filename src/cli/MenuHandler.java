package cli;

import model.*;
import model.enums.OrderStatus;
import repository.UserRepository;
import service.*;

import java.util.*;

public class MenuHandler {
    private final Scanner scanner;
    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;
    private final CouponService couponService;
    private final LoggingService loggingService;
    private final EventService eventService;
    private final FailureService failureService;
    private final UserRepository userRepository;

    private String currentUserId = "USER_1";

    public MenuHandler(Scanner scanner, ProductService productService, CartService cartService,
                       OrderService orderService, CouponService couponService,
                       LoggingService loggingService, EventService eventService,
                       FailureService failureService, UserRepository userRepository) {
        this.scanner = scanner;
        this.productService = productService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.couponService = couponService;
        this.loggingService = loggingService;
        this.eventService = eventService;
        this.failureService = failureService;
        this.userRepository = userRepository;
    }

    public void run() {
        printBanner();
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter choice: ");
            System.out.println();
            switch (choice) {
                case 1  -> handleAddProduct();
                case 2  -> productService.viewProducts();
                case 3  -> handleAddToCart();
                case 4  -> handleRemoveFromCart();
                case 5  -> cartService.viewCart(currentUserId);
                case 6  -> handleApplyCoupon();
                case 7  -> handlePlaceOrder();
                case 8  -> handleCancelOrder();
                case 9  -> handleViewOrders();
                case 10 -> productService.showLowStockAlerts();
                case 11 -> handleReturnProduct();
                case 12 -> handleSimulateConcurrentUsers();
                case 13 -> loggingService.printAllLogs();
                case 14 -> handleTriggerFailureMode();
                case 0  -> { running = false; System.out.println("  👋 Exiting. Goodbye!"); }
                default -> System.out.println("  ❌ Invalid option. Please try again.");
            }
            System.out.println();
        }
    }

    private void printBanner() {
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║       🛒  DISTRIBUTED E-COMMERCE ORDER ENGINE  🛒             ║");
        System.out.println("║              Hackathon Technical Assessment                   ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println("  Current User: " + currentUserId);
        System.out.println("  Type a number and press Enter to select an option.\n");
    }

    private void printMenu() {
        System.out.println("  ════════════════════════════════════");
        System.out.printf("  Current User: %s%n", currentUserId);
        System.out.println("  ════════════════════════════════════");
        System.out.println("   1. Add Product");
        System.out.println("   2. View Products");
        System.out.println("   3. Add to Cart");
        System.out.println("   4. Remove from Cart");
        System.out.println("   5. View Cart");
        System.out.println("   6. Apply Coupon");
        System.out.println("   7. Place Order");
        System.out.println("   8. Cancel Order");
        System.out.println("   9. View Orders");
        System.out.println("  10. Low Stock Alert");
        System.out.println("  11. Return Product");
        System.out.println("  12. Simulate Concurrent Users");
        System.out.println("  13. View Logs");
        System.out.println("  14. Trigger Failure Mode");
        System.out.println("   0. Exit");
        System.out.println("  ────────────────────────────────────");
        System.out.print("  [Switch user: type 'u <userId>'] ");
    }

    private void handleAddProduct() {
        System.out.println("  ── Add New Product ──");
        String name  = readString("  Product Name  : ");
        double price = readDouble("  Price (₹)     : ");
        int stock    = readInt("  Stock Quantity: ");
        productService.addProduct(name, price, stock);
    }

    private void handleAddToCart() {
        System.out.println("  ── Add to Cart ──");
        productService.viewProducts();
        String productId = readString("  Product ID: ").toUpperCase();
        int qty = readInt("  Quantity  : ");
        cartService.addToCart(currentUserId, productId, qty);
    }

    private void handleRemoveFromCart() {
        cartService.viewCart(currentUserId);
        String productId = readString("  Product ID to remove: ").toUpperCase();
        cartService.removeFromCart(currentUserId, productId);
    }

    private void handleApplyCoupon() {
        System.out.println("  ── Apply Coupon ──");
        System.out.println("  Available coupons: SAVE10 (10% off), FLAT200 (₹200 off)");
        cartService.viewCart(currentUserId);
        String code = readString("  Coupon Code: ").toUpperCase();
        couponService.applyCoupon(currentUserId, code);
    }

    private void handlePlaceOrder() {
        System.out.println("  ── Place Order ──");
        cartService.viewCart(currentUserId);
        String confirm = readString("  Confirm order? (yes/no): ");
        if (confirm.equalsIgnoreCase("yes") || confirm.equalsIgnoreCase("y")) {
            orderService.placeOrder(currentUserId);
        } else {
            System.out.println("  Order cancelled by user.");
        }
    }

    private void handleCancelOrder() {
        System.out.println("  ── Cancel Order ──");
        orderService.viewOrders(currentUserId);
        String orderId = readString("  Order ID to cancel: ").toUpperCase();
        orderService.cancelOrder(orderId, currentUserId);
    }

    private void handleViewOrders() {
        System.out.println("  1. My Orders    2. All Orders    3. Search by ID    4. Filter by Status");
        int sub = readInt("  Choice: ");
        switch (sub) {
            case 1 -> orderService.viewOrders(currentUserId);
            case 2 -> orderService.viewAllOrders();
            case 3 -> {
                String id = readString("  Order ID: ").toUpperCase();
                Order o = orderService.getOrder(id);
                if (o != null) System.out.println(o);
                else System.out.println("  Order not found.");
            }
            case 4 -> {
                System.out.println("  Statuses: CREATED, PENDING_PAYMENT, PAID, SHIPPED, DELIVERED, FAILED, CANCELLED");
                String s = readString("  Status: ").toUpperCase();
                try {
                    OrderStatus status = OrderStatus.valueOf(s);
                    orderService.getAllOrders().stream()
                            .filter(o -> o.getStatus() == status)
                            .forEach(o -> System.out.printf("  [%s] User=%-8s Total=₹%.2f%n",
                                    o.getOrderId(), o.getUserId(), o.getTotal()));
                } catch (IllegalArgumentException e) {
                    System.out.println("  ❌ Invalid status.");
                }
            }
            default -> System.out.println("  Invalid option.");
        }
    }

    private void handleReturnProduct() {
        System.out.println("  ── Return Product ──");
        orderService.viewOrders(currentUserId);
        String orderId   = readString("  Order ID  : ").toUpperCase();
        String productId = readString("  Product ID: ").toUpperCase();
        int qty          = readInt("  Quantity  : ");
        orderService.returnProduct(orderId, currentUserId, productId, qty);
    }

    private void handleSimulateConcurrentUsers() {
        System.out.println("  ── Simulate Concurrent Users ──");
        productService.viewProducts();
        String productId = readString("  Product ID : ").toUpperCase();
        int numUsers = readInt("  Num Users  : ");
        int qty      = readInt("  Qty Each   : ");
        orderService.simulateConcurrentUsers(productId, numUsers, qty);
    }

    private void handleTriggerFailureMode() {
        failureService.printStatus();
        System.out.println("\n  1. Toggle Payment Failure");
        System.out.println("  2. Toggle Order Creation Failure");
        System.out.println("  3. Toggle Inventory Failure");
        System.out.println("  4. Enable ALL Failures");
        System.out.println("  5. Disable ALL Failures");
        int sub = readInt("  Choice: ");
        switch (sub) {
            case 1 -> failureService.setPaymentFailure(!failureService.isGlobalFailureMode());
            case 2 -> failureService.setOrderCreationFailure(true);
            case 3 -> failureService.setInventoryFailure(true);
            case 4 -> { failureService.setGlobalFailureMode(true); System.out.println("  ⚡ All failures ENABLED"); }
            case 5 -> { failureService.setGlobalFailureMode(false); System.out.println("  ✅ All failures DISABLED"); }
            default -> System.out.println("  Invalid option.");
        }
        failureService.printStatus();
    }

    // ── Input helpers ──────────────────────────────────────────────────────────

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            // Allow user-switch command inline
            if (line.startsWith("u ")) {
                switchUser(line.substring(2).trim());
                continue;
            }
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("  ❌ Please enter a valid number.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("  ❌ Please enter a valid number.");
            }
        }
    }

    private String readString(String prompt) {
        System.out.print(prompt);
        String line = scanner.nextLine().trim();
        if (line.startsWith("u ")) {
            switchUser(line.substring(2).trim());
            return readString(prompt);
        }
        return line;
    }

    private void switchUser(String userId) {
        if (!userRepository.exists(userId)) {
            userRepository.save(new User(userId, "User " + userId));
            System.out.println("  👤 New user created: " + userId);
        }
        currentUserId = userId;
        System.out.println("  👤 Switched to user: " + currentUserId);
    }
}
