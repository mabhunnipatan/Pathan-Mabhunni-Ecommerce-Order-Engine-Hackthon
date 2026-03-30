package repository;

import model.Product;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ProductRepository {
    private final ConcurrentHashMap<String, Product> products = new ConcurrentHashMap<>();

    public boolean exists(String id) { return products.containsKey(id); }

    public void save(Product product) { products.put(product.getId(), product); }

    public Product findById(String id) { return products.get(id); }

    public Collection<Product> findAll() { return products.values(); }

    public void delete(String id) { products.remove(id); }

    public int count() { return products.size(); }
}
