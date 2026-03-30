package repository;

import model.User;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class UserRepository {
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    public void save(User user) { users.put(user.getUserId(), user); }

    public User findById(String userId) { return users.get(userId); }

    public Collection<User> findAll() { return users.values(); }

    public boolean exists(String userId) { return users.containsKey(userId); }
}
