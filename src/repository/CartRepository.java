package repository;

import model.Cart;
import java.util.concurrent.ConcurrentHashMap;

public class CartRepository {
    private final ConcurrentHashMap<String, Cart> carts = new ConcurrentHashMap<>();

    public Cart getOrCreate(String userId) {
        return carts.computeIfAbsent(userId, Cart::new);
    }

    public Cart findByUserId(String userId) { return carts.get(userId); }

    public void save(Cart cart) { carts.put(cart.getUserId(), cart); }

    public void delete(String userId) { carts.remove(userId); }
}
