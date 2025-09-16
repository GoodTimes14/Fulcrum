package it.raniero.fulcrum.database.lock.impl;

import it.raniero.fulcrum.database.lock.ILockManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseLockManager<T> implements ILockManager<T> {

    private final Map<T, Object> locks = new ConcurrentHashMap<>();

    private final Map<T, Integer> workingCounts = new ConcurrentHashMap<>();

    @Override
    public Object lockObject(T id) {

        if (workingCounts.containsKey(id)) {

            workingCounts.put(id, workingCounts.get(id) + 1);
            return locks.get(id);

        } else {

            Object lock = new Object();

            workingCounts.put(id, 1);
            locks.put(id, lock);
            return lock;
        }
    }

    @Override
    public void unlockObject(T id) {
        int workingCount = workingCounts.get(id);
        if (--workingCount == 0) {

            Object lock = locks.remove(id);

            synchronized (lock) {
                lock.notify();
            }

            workingCounts.remove(id);

        } else {
            workingCounts.put(id, workingCount);
        }
    }

    @Override
    public Object getLock(T id) {
        return locks.get(id);
    }

    @Override
    public boolean isLocked(T id) {
        return locks.containsKey(id);
    }
}
