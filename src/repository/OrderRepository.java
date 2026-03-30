package repository;

import model.Order;
import model.enums.OrderStatus;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class OrderRepository {
    private final ConcurrentHashMap<String, Order> orders = new ConcurrentHashMap<>();

    public void save(Order order) { orders.put(order.getOrderId(), order); }

    public Order findById(String orderId) { return orders.get(orderId); }

    public Collection<Order> findAll() { return orders.values(); }

    public List<Order> findByUserId(String userId) {
        return orders.values().stream()
                .filter(o -> o.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public List<Order> findByStatus(OrderStatus status) {
        return orders.values().stream()
                .filter(o -> o.getStatus() == status)
                .collect(Collectors.toList());
    }

    public boolean existsByIdempotencyKey(String key) {
        return orders.values().stream()
                .anyMatch(o -> key.equals(o.getIdempotencyKey()));
    }

    public int count() { return orders.size(); }
}
