package service;

import model.*;
import model.enums.OrderStatus;
import repository.*;
import util.IdGenerator;

import java.util.*;
import java.util.stream.Collectors;

public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final CouponService couponService;
    private final FraudService fraudService;
    private final EventService eventService;
    private final LoggingService loggingService;
    private final FailureService failureService;

    // Idempotency: userId -> last orderKey
    private final Map<String, String> idempotencyStore = new HashMap<>();

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository,
                        UserRepository userRepository, InventoryService inventoryService,
                        PaymentService paymentService, CouponService couponService,
                        FraudService fraudService, EventService eventService,
                        LoggingService loggingService, FailureService failureService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.couponService = couponService;
        this.fraudService = fraudService;
        this.eventService = eventService;
        this.loggingService = loggingService;
        this.failureService = failureService;
    }

    public Order placeOrder(String userId) {
        // Idempotency check
        String idemKey = userId + "_" + System.currentTimeMillis() / 1000;
        if (orderRepository.existsByIdempotencyKey(idemKey)) {
            System.out.println("  ⚠️  Duplicate order request detected. Ignoring.");
            return null;
        }

        Cart cart = cartRepository.getOrCreate(userId);
        if (cart.isEmpty()) {
            System.out.println("  ❌ Cart is empty. Add items before placing an order.");
            return null;
        }

        User user = userRepository.findById(userId);
        if (user == null) {
            System.out.println("  ❌ User not found: " + userId);
            return null;
        }

        // Step 1: Apply auto discounts if no coupon
        if (cart.getAppliedCoupon() == null) {
            double autoDiscount = couponService.applyAutoDiscountsOnly(cart);
            if (autoDiscount > 0) {
                System.out.printf("  💡 Auto-discount applied: ₹%.2f%n", autoDiscount);
            }
        }

        double total = cart.getTotal();
        String orderId = IdGenerator.generateOrderId();

        System.out.println("\n  🛒 Placing order " + orderId + " for " + userId + "...");

        // Step 2: Failure injection check
        if (failureService.shouldFailOrderCreation()) {
            System.out.println("  ⚡ [FAILURE INJECTION] Order creation failed!");
            rollback(userId, cart, null, "order_creation_failure");
            return null;
        }

        // Step 3: Build order items
        List<OrderItem> orderItems = cart.getItemList().stream()
                .map(ci -> new OrderItem(ci.getProductId(), ci.getProductName(), ci.getPrice(), ci.getQuantity()))
                .collect(Collectors.toList());

        // Step 4: Create order
        Order order = new Order(orderId, userId, orderItems,
                cart.getSubtotal(), cart.getDiscountAmount(), cart.getAppliedCoupon());
        order.setIdempotencyKey(idemKey);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        orderRepository.save(order);
        loggingService.logOrder(orderId, "created for " + userId);

        // Step 5: Fraud check
        fraudService.checkFraud(userId, total);
        user.recordOrder();

        // Step 6: Process payment
        if (failureService.shouldFailPayment()) {
            System.out.println("  ⚡ [FAILURE INJECTION] Payment failed!");
            rollback(userId, cart, order, "payment_failure");
            return null;
        }

        Payment payment = paymentService.processPayment(orderId, userId, total);
        if (payment.getStatus().name().equals("FAILED")) {
            rollback(userId, cart, order, "payment_failure");
            return null;
        }

        // Step 7: Inventory failure check
        if (failureService.shouldFailInventory()) {
            System.out.println("  ⚡ [FAILURE INJECTION] Inventory update failed!");
            rollback(userId, cart, order, "inventory_failure");
            return null;
        }

        // Step 8: Confirm stock deductions
        for (CartItem item : cart.getItemList()) {
            inventoryService.confirmDeduction(item.getProductId(), userId, item.getQuantity());
        }

        // Step 9: Update order status
        order.setStatus(OrderStatus.PAID);
        loggingService.logOrder(orderId, "PAID total=₹" + total);

        // Step 10: Clear cart
        cart.clear();

        // Step 11: Publish events
        eventService.processOrderEvents(orderId, userId, total);

        System.out.println("\n  ✅ Order placed successfully!");
        System.out.println(order);
        return order;
    }

    private void rollback(String userId, Cart cart, Order order, String reason) {
        System.out.println("  🔄 Rolling back transaction: " + reason);

        // Restore stock reservations
        if (cart != null) {
            for (CartItem item : cart.getItemList()) {
                inventoryService.releaseReservation(item.getProductId(), userId, item.getQuantity());
            }
            System.out.println("  🔄 Stock reservations released.");
        }

        // Mark order as failed
        if (order != null) {
            order.setStatus(OrderStatus.FAILED);
            loggingService.logOrder(order.getOrderId(), "FAILED reason=" + reason);
            System.out.println("  🔄 Order " + order.getOrderId() + " marked as FAILED.");
        }

        System.out.println("  ✅ Rollback complete.");
    }

    public boolean cancelOrder(String orderId, String userId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            System.out.println("  ❌ Order not found: " + orderId);
            return false;
        }
        if (!order.getUserId().equals(userId)) {
            System.out.println("  ❌ Order does not belong to user: " + userId);
            return false;
        }

        // Edge case: cannot cancel already cancelled order
        if (order.getStatus() == OrderStatus.CANCELLED) {
            System.out.println("  ❌ Order is already cancelled.");
            return false;
        }
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.SHIPPED) {
            System.out.println("  ❌ Cannot cancel an order that is " + order.getStatus());
            return false;
        }

        // Restore stock
        for (OrderItem item : order.getItems()) {
            inventoryService.restoreStock(item.getProductId(), item.getActiveQuantity());
        }

        // Issue refund if order was already paid
        boolean wasPaid = order.getStatus() == OrderStatus.PAID;

        order.setStatus(OrderStatus.CANCELLED);
        loggingService.logOrder(orderId, "CANCELLED by " + userId);

        if (wasPaid) {
            paymentService.refund(orderId, userId, order.getTotal());
        }

        System.out.println("  ✅ Order " + orderId + " cancelled and stock restored.");
        return true;
    }

    public boolean returnProduct(String orderId, String userId, String productId, int qty) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            System.out.println("  ❌ Order not found: " + orderId);
            return false;
        }
        if (!order.getUserId().equals(userId)) {
            System.out.println("  ❌ Order does not belong to user.");
            return false;
        }
        if (order.getStatus() != OrderStatus.DELIVERED && order.getStatus() != OrderStatus.PAID) {
            System.out.println("  ❌ Can only return delivered/paid orders. Status: " + order.getStatus());
            return false;
        }

        Optional<OrderItem> itemOpt = order.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst();
        if (itemOpt.isEmpty()) {
            System.out.println("  ❌ Product not found in order.");
            return false;
        }

        OrderItem item = itemOpt.get();
        if (!item.returnItems(qty)) {
            System.out.println("  ❌ Cannot return more than active quantity (" + item.getActiveQuantity() + ")");
            return false;
        }

        // Restore stock
        inventoryService.restoreStock(productId, qty);
        order.recalculateTotal();

        double refundAmount = item.getPrice() * qty;
        paymentService.refund(orderId, userId, refundAmount);

        loggingService.log(userId + " returned " + productId + " qty=" + qty + " from " + orderId);
        System.out.printf("  ✅ Returned %d unit(s) of %s. Refund: ₹%.2f%n", qty, productId, refundAmount);
        return true;
    }

    public void viewOrders(String userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        System.out.println("\n  ┌─────────────────────────────────────────────────────────────┐");
        System.out.printf("  │  Orders for %-47s│%n", userId);
        System.out.println("  └─────────────────────────────────────────────────────────────┘");
        if (orders.isEmpty()) {
            System.out.println("  No orders found.");
            return;
        }
        orders.forEach(o -> {
            System.out.printf("  [%s] Status=%-15s Total=₹%.2f Items=%d%n",
                    o.getOrderId(), o.getStatus(), o.getTotal(), o.getItems().size());
        });
    }

    public void viewAllOrders() {
        Collection<Order> orders = orderRepository.findAll();
        System.out.println("\n  ┌─────────────────────────────────────────────────────────────┐");
        System.out.println("  │                     ALL ORDERS                             │");
        System.out.println("  └─────────────────────────────────────────────────────────────┘");
        if (orders.isEmpty()) {
            System.out.println("  No orders found.");
            return;
        }
        orders.forEach(o -> System.out.printf("  [%s] User=%-8s Status=%-15s Total=₹%.2f%n",
                o.getOrderId(), o.getUserId(), o.getStatus(), o.getTotal()));
        System.out.println("  Total: " + orders.size() + " orders");
    }

    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId);
    }

    public Collection<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public void simulateConcurrentUsers(String productId, int numUsers, int qtyEach) {
        System.out.println("\n  🔀 Simulating " + numUsers + " concurrent users trying to add " + qtyEach + " units each...");

        List<Thread> threads = new ArrayList<>();
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        for (int i = 1; i <= numUsers; i++) {
            String uid = "USER_" + i;
            if (!userRepository.exists(uid)) {
                userRepository.save(new User(uid, "User " + i));
            }
            threads.add(new Thread(() -> {
                boolean reserved = false;
                try {
                    reserved = inventoryService.reserveStock(productId, uid, qtyEach);
                } catch (Exception e) {
                    reserved = false;
                }
                results.add(uid + ": " + (reserved ? "✅ SUCCESS" : "❌ FAILED"));
            }));
        }

        threads.forEach(Thread::start);
        threads.forEach(t -> {
            try { t.join(); } catch (InterruptedException ignored) {}
        });

        System.out.println("  Results:");
        results.forEach(r -> System.out.println("    " + r));
    }
}
