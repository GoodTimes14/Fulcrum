package it.raniero.fulcrum.api.database.lock.impl;

import it.raniero.fulcrum.api.database.lock.ILockManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseLockManager<T> implements ILockManager<T> {

    private static final class LockHolder {
        final Object lock = new Object();
        int count;
    }

    private final Map<T, LockHolder> holders = new ConcurrentHashMap<>();

    @Override
    public Object lockObject(T id) {
        LockHolder holder = holders.compute(id, (key, existing) -> {
            LockHolder current = existing != null ? existing : new LockHolder();
            current.count++;
            return current;
        });
        return holder.lock;
    }

    @Override
    public void unlockObject(T id) {
        holders.compute(id, (key, existing) -> {
            if (existing == null) {
                return null;
            }

            existing.count--;
            if (existing.count > 0) {
                return existing;
            }

            synchronized (existing.lock) {
                existing.lock.notifyAll();
            }
            return null;
        });
    }

    @Override
    public Object getLock(T id) {
        LockHolder holder = holders.get(id);
        return holder != null ? holder.lock : null;
    }

    @Override
    public boolean isLocked(T id) {
        return holders.containsKey(id);
    }
}
