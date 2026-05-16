package it.raniero.fulcrum.api.database.lock;

/**
 * Manages lock objects keyed by an identifier.
 *
 * @param <T> lock identifier type
 */
public interface ILockManager<T> {

    /**
     * Locks the object identified by the id.
     *
     * @param id lock identifier
     * @return lock object
     */
    Object lockObject(T id);

    /**
     * Checks whether an id is currently locked.
     *
     * @param id lock identifier
     * @return {@code true} when locked
     */
    boolean isLocked(T id);

    /**
     * Unlocks the object identified by the id.
     *
     * @param id lock identifier
     */
    void unlockObject(T id);

    /**
     * Gets the lock object associated with the id.
     *
     * @param id lock identifier
     * @return lock object, or {@code null} when no lock exists
     */
    Object getLock(T id);
}
