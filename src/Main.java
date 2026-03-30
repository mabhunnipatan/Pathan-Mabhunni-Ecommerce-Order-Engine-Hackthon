import cli.MenuHandler;
import model.User;
import repository.*;
import service.*;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // ── Repositories ──────────────────────────────────────────────────────
        ProductRepository productRepository = new ProductRepository();
        CartRepository    cartRepository    = new CartRepository();
        OrderRepository   orderRepository   = new OrderRepository();
        UserRepository    userRepository    = new UserRepository();

        // ── Default users ─────────────────────────────────────────────────────
        userRepository.save(new User("USER_1", "Alice"));
        userRepository.save(new User("USER_2", "Bob"));
        userRepository.save(new User("USER_3", "Charlie"));

        // ── Services ──────────────────────────────────────────────────────────
        LoggingService   loggingService   = new LoggingService();
        FailureService   failureService   = new FailureService();
        InventoryService inventoryService = new InventoryService(productRepository, loggingService);
        EventService     eventService     = new EventService(loggingService);
        CouponService    couponService    = new CouponService(cartRepository, loggingService);
        PaymentService   paymentService   = new PaymentService(loggingService);
        FraudService     fraudService     = new FraudService(userRepository, loggingService);
        ProductService   productService   = new ProductService(productRepository, loggingService);
        CartService      cartService      = new CartService(cartRepository, productRepository, inventoryService, loggingService);
        OrderService     orderService     = new OrderService(
                orderRepository, cartRepository, userRepository,
                inventoryService, paymentService, couponService,
                fraudService, eventService, loggingService, failureService);

        // ── Seed some products ────────────────────────────────────────────────
        productService.addProduct("iPhone 15",      79999.00, 10);
        productService.addProduct("Samsung Galaxy", 54999.00, 5);
        productService.addProduct("OnePlus 12",     42999.00, 8);
        productService.addProduct("USB-C Cable",      499.00, 3);
        productService.addProduct("Wireless Mouse",  1299.00, 20);

        // ── CLI ───────────────────────────────────────────────────────────────
        Scanner scanner = new Scanner(System.in);
        MenuHandler menu = new MenuHandler(scanner, productService, cartService, orderService,
                couponService, loggingService, eventService, failureService, userRepository);
        menu.run();
        scanner.close();
    }
}
