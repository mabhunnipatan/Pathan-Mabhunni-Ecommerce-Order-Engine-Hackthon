package service;

import model.Product;
import repository.ProductRepository;
import util.LockManager;

import java.util.HashMap;
import java.util.Map;

public class InventoryService {
    private final ProductRepository productRepository;
    private final LoggingService loggingService;
    // Track reservation expiry: productId -> {userId -> {qty, expiryTime}}
    private final Map<String, Map<String, long[]>> reservations = new HashMap<>();

    public InventoryService(ProductRepository productRepository, LoggingService loggingService) {
        this.productRepository = productRepository;
        this.loggingService = loggingService;
    }

    public boolean reserveStock(String productId, String userId, int qty) {
        LockManager.lock(productId);
        try {
            Product product = productRepository.findById(productId);
            if (product == null) return false;
            if (product.getAvailableStock() < qty) {
                System.out.println("  ❌ Insufficient stock for " + productId +
                        " (Available: " + product.getAvailableStock() + ", Requested: " + qty + ")");
                return false;
            }
            boolean reserved = product.reserveStock(qty);
            if (reserved) {
                // Track expiry
                reservations.computeIfAbsent(productId, k -> new HashMap<>())
                        .put(userId, new long[]{qty, System.currentTimeMillis() + 300_000L});
                loggingService.log(userId + " reserved " + productId + " qty=" + qty);
            }
            return reserved;
        } finally {
            LockManager.unlock(productId);
        }
    }

    public void releaseReservation(String productId, String userId, int qty) {
        LockManager.lock(productId);
        try {
            Product product = productRepository.findById(productId);
            if (product != null) {
                product.releaseReservedStock(qty);
                Map<String, long[]> userMap = reservations.get(productId);
                if (userMap != null) userMap.remove(userId);
                loggingService.log(userId + " released reservation " + productId + " qty=" + qty);
            }
        } finally {
            LockManager.unlock(productId);
        }
    }

    public void confirmDeduction(String productId, String userId, int qty) {
        LockManager.lock(productId);
        try {
            Product product = productRepository.findById(productId);
            if (product != null) {
                product.deductStock(qty);
                Map<String, long[]> userMap = reservations.get(productId);
                if (userMap != null) userMap.remove(userId);
                loggingService.log("INVENTORY_UPDATED " + productId + " deducted qty=" + qty);
            }
        } finally {
            LockManager.unlock(productId);
        }
    }

    public void restoreStock(String productId, int qty) {
        LockManager.lock(productId);
        try {
            Product product = productRepository.findById(productId);
            if (product != null) {
                product.restoreStock(qty);
                loggingService.log("INVENTORY_UPDATED " + productId + " restored qty=" + qty);
            }
        } finally {
            LockManager.unlock(productId);
        }
    }

    public void checkAndExpireReservations() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Map<String, long[]>> productEntry : reservations.entrySet()) {
            String productId = productEntry.getKey();
            Map<String, long[]> userMap = productEntry.getValue();
            userMap.entrySet().removeIf(entry -> {
                long[] data = entry.getValue();
                if (now > data[1]) {
                    releaseReservation(productId, entry.getKey(), (int) data[0]);
                    System.out.println("  ⏰ Reservation expired for " + productId + " user=" + entry.getKey());
                    return true;
                }
                return false;
            });
        }
    }
}
