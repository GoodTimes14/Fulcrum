package it.raniero.fulcrum.database.lock;

public interface ILockManager<T> {

    Object lockObject(T id);

    boolean isLocked(T id);

    void unlockObject(T id);

    Object getLock(T id);
}
