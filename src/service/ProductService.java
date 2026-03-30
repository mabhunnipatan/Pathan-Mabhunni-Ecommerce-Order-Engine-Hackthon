package service;

import model.Product;
import repository.ProductRepository;
import util.Constants;
import util.IdGenerator;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ProductService {
    private final ProductRepository productRepository;
    private final LoggingService loggingService;

    public ProductService(ProductRepository productRepository, LoggingService loggingService) {
        this.productRepository = productRepository;
        this.loggingService = loggingService;
    }

    public Product addProduct(String name, double price, int stock) {
        if (name == null || name.isBlank()) {
            System.out.println("  ❌ Product name cannot be empty.");
            return null;
        }
        if (price < 0) {
            System.out.println("  ❌ Price cannot be negative.");
            return null;
        }
        if (stock < 0) {
            System.out.println("  ❌ Stock cannot be negative.");
            return null;
        }
        String id = IdGenerator.generateProductId();
        Product product = new Product(id, name, price, stock);
        productRepository.save(product);
        loggingService.log("SYSTEM added product " + id + " name=" + name + " price=" + price + " stock=" + stock);
        System.out.println("  ✅ Product added: " + product);
        return product;
    }

    public void viewProducts() {
        Collection<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            System.out.println("  No products available.");
            return;
        }
        System.out.println("\n  ┌─────────────────────────────────────────────────────────────┐");
        System.out.println("  │                      PRODUCT CATALOG                       │");
        System.out.println("  └─────────────────────────────────────────────────────────────┘");
        products.forEach(p -> System.out.println("  " + p));
    }

    public Product getProduct(String id) {
        return productRepository.findById(id);
    }

    public boolean productExists(String id) {
        return productRepository.exists(id);
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findAll().stream()
                .filter(p -> p.getAvailableStock() > 0 && p.getAvailableStock() <= Constants.LOW_STOCK_THRESHOLD)
                .collect(Collectors.toList());
    }

    public List<Product> getOutOfStockProducts() {
        return productRepository.findAll().stream()
                .filter(p -> p.getAvailableStock() == 0)
                .collect(Collectors.toList());
    }

    public void showLowStockAlerts() {
        List<Product> lowStock = getLowStockProducts();
        List<Product> outOfStock = getOutOfStockProducts();

        System.out.println("\n  ┌─────────────────────────────────────────────────────────────┐");
        System.out.println("  │                   INVENTORY ALERT SYSTEM                   │");
        System.out.println("  └─────────────────────────────────────────────────────────────┘");

        if (!outOfStock.isEmpty()) {
            System.out.println("  🚫 OUT OF STOCK:");
            outOfStock.forEach(p -> System.out.println("     " + p));
        }
        if (!lowStock.isEmpty()) {
            System.out.println("  ⚠️  LOW STOCK (≤" + Constants.LOW_STOCK_THRESHOLD + " units):");
            lowStock.forEach(p -> System.out.println("     " + p));
        }
        if (outOfStock.isEmpty() && lowStock.isEmpty()) {
            System.out.println("  ✅ All products have sufficient stock.");
        }
    }
}
