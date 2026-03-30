package util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class LockManager {
    private static final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public static ReentrantLock getLock(String key) {
        return locks.computeIfAbsent(key, k -> new ReentrantLock(true));
    }

    public static boolean tryLock(String key) {
        return getLock(key).tryLock();
    }

    public static void lock(String key) {
        getLock(key).lock();
    }

    public static void unlock(String key) {
        ReentrantLock lock = locks.get(key);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    public static boolean isLocked(String key) {
        ReentrantLock lock = locks.get(key);
        return lock != null && lock.isLocked();
    }
}
