package it.raniero.fulcrum.database.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.raniero.fulcrum.api.database.lock.impl.DatabaseLockManager;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class DatabaseLockManagerTest {

    @Test
    void lockObjectReturnsLockAndMarksLocked() {
        DatabaseLockManager<String> manager = new DatabaseLockManager<>();

        Object lock = manager.lockObject("user-42");

        assertThat(lock).isNotNull();
        assertThat(manager.isLocked("user-42")).isTrue();
        assertThat(manager.getLock("user-42")).isSameAs(lock);
    }

    @Test
    void unknownIdIsNotLocked() {
        DatabaseLockManager<String> manager = new DatabaseLockManager<>();

        assertThat(manager.isLocked("nope")).isFalse();
        assertThat(manager.getLock("nope")).isNull();
    }

    @Test
    void reentrantLockReturnsSameInstanceAndStaysHeldUntilFullyReleased() {
        DatabaseLockManager<String> manager = new DatabaseLockManager<>();

        Object first = manager.lockObject("id");
        Object second = manager.lockObject("id");

        assertThat(second).isSameAs(first);

        manager.unlockObject("id");
        assertThat(manager.isLocked("id"))
                .as("still held after one release because refcount was 2")
                .isTrue();
        assertThat(manager.getLock("id")).isSameAs(first);

        manager.unlockObject("id");
        assertThat(manager.isLocked("id")).isFalse();
        assertThat(manager.getLock("id")).isNull();
    }

    @Test
    void differentIdsGetDifferentLocks() {
        DatabaseLockManager<String> manager = new DatabaseLockManager<>();

        Object a = manager.lockObject("a");
        Object b = manager.lockObject("b");

        assertThat(a).isNotSameAs(b);
        assertThat(manager.isLocked("a")).isTrue();
        assertThat(manager.isLocked("b")).isTrue();
    }

    @Test
    void unlockOfUnknownIdIsNoOp() {
        DatabaseLockManager<String> manager = new DatabaseLockManager<>();

        manager.unlockObject("ghost");

        assertThat(manager.isLocked("ghost")).isFalse();
    }

    @Test
    void reacquiringAfterFullReleaseProducesAFreshLockInstance() {
        DatabaseLockManager<String> manager = new DatabaseLockManager<>();

        Object first = manager.lockObject("id");
        manager.unlockObject("id");
        Object second = manager.lockObject("id");

        assertThat(second).isNotSameAs(first);
        manager.unlockObject("id");
    }

    /**
     * Reproduces the race the old containsKey+put implementation could lose:
     * if N threads call lockObject(sameId) before any of them release, they
     * MUST all observe the same lock instance. The old code could hand out
     * distinct Object instances to interleaved callers.
     */
    @Test
    void concurrentOverlappingLocksObserveASingleInstance() throws Exception {
        int threads = 64;
        DatabaseLockManager<String> manager = new DatabaseLockManager<>();
        Set<Object> distinctLocks = ConcurrentHashMap.newKeySet();

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch acquired = new CountDownLatch(threads);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        try {
            for (int i = 0; i < threads; i++) {
                pool.submit(() -> {
                    try {
                        start.await();
                        Object lock = manager.lockObject("hot-id");
                        distinctLocks.add(lock);
                        acquired.countDown();
                        release.await();
                        manager.unlockObject("hot-id");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }

            start.countDown();
            assertTrue(acquired.await(10, TimeUnit.SECONDS), "threads did not acquire in time");

            assertThat(distinctLocks)
                    .as("all concurrent holders must see the same lock object")
                    .hasSize(1);
            assertThat(manager.isLocked("hot-id")).isTrue();

            release.countDown();
        } finally {
            pool.shutdown();
            assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS), "executor did not terminate");
        }

        assertThat(manager.isLocked("hot-id"))
                .as("all refs released -> id must be cleaned up")
                .isFalse();
    }

    /**
     * Hammers lock/unlock cycles from many threads on the same id; verifies the
     * refcount never goes negative and that the id is fully released at the end.
     */
    @Test
    void repeatedLockUnlockCyclesLeaveNoLeakedState() throws Exception {
        int threads = 16;
        int iterations = 5_000;
        DatabaseLockManager<String> manager = new DatabaseLockManager<>();

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        try {
            for (int i = 0; i < threads; i++) {
                pool.submit(() -> {
                    try {
                        start.await();
                        for (int j = 0; j < iterations; j++) {
                            manager.lockObject("churn");
                            manager.unlockObject("churn");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }

            start.countDown();
            assertTrue(done.await(30, TimeUnit.SECONDS), "workers did not finish in time");
        } finally {
            pool.shutdown();
            assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        }

        assertThat(manager.isLocked("churn"))
                .as("balanced lock/unlock pairs must clean up the holder")
                .isFalse();
    }

    @Test
    void unlockNotifiesWaiters() throws Exception {
        DatabaseLockManager<String> manager = new DatabaseLockManager<>();
        Object lock = manager.lockObject("notify-id");

        CountDownLatch waiterStarted = new CountDownLatch(1);
        CountDownLatch waiterDone = new CountDownLatch(1);

        Thread waiter = new Thread(() -> {
            synchronized (lock) {
                waiterStarted.countDown();
                try {
                    lock.wait(5_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            waiterDone.countDown();
        });
        waiter.start();

        assertTrue(waiterStarted.await(2, TimeUnit.SECONDS), "waiter never started");
        // give the waiter a moment to actually enter wait()
        Thread.sleep(50);

        manager.unlockObject("notify-id");

        assertTrue(waiterDone.await(5, TimeUnit.SECONDS), "waiter was not notified on unlock");
        waiter.join(1_000);
    }
}