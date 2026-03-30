package service;

import model.User;
import repository.UserRepository;
import util.Constants;

import java.util.List;

public class FraudService {
    private final UserRepository userRepository;
    private final LoggingService loggingService;

    public FraudService(UserRepository userRepository, LoggingService loggingService) {
        this.userRepository = userRepository;
        this.loggingService = loggingService;
    }

    public boolean checkFraud(String userId, double orderTotal) {
        User user = userRepository.findById(userId);
        if (user == null) return false;

        // Rule 1: High-value order → suspicious
        if (orderTotal > Constants.HIGH_VALUE_ORDER_THRESHOLD) {
            loggingService.log("FRAUD_ALERT high-value order by " + userId + " amount=₹" + orderTotal);
            System.out.printf("  ⚠️  Fraud Alert: High-value order (₹%.2f) by %s - flagging as suspicious%n",
                    orderTotal, userId);
            user.setFlagged(true);
            return true;
        }

        // Rule 2: 3 orders in 1 minute → flag user
        long now = System.currentTimeMillis();
        List<Long> recentOrders = user.getOrderTimestamps().stream()
                .filter(t -> now - t < Constants.FRAUD_WINDOW_MS)
                .toList();

        if (recentOrders.size() >= Constants.FRAUD_ORDER_COUNT) {
            loggingService.log("FRAUD_ALERT rapid orders by " + userId + " count=" + recentOrders.size());
            System.out.println("  ⚠️  Fraud Alert: " + userId + " placed " + recentOrders.size()
                    + " orders in 1 minute - flagging user");
            user.setFlagged(true);
            return true;
        }

        return false;
    }

    public boolean isUserFlagged(String userId) {
        User user = userRepository.findById(userId);
        return user != null && user.isFlagged();
    }
}
