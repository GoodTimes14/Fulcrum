package it.raniero.fulcrum.database.redis.loqui.discovery;

import it.raniero.fulcrum.api.database.loqui.discovery.ILoquiDiscovery;
import it.raniero.fulcrum.api.database.loqui.discovery.LoquiDiscoveryData;
import it.raniero.fulcrum.api.database.loqui.discovery.LoquiDiscoveryOptions;
import it.raniero.fulcrum.api.database.loqui.discovery.LoquiDiscoveryStore;
import it.raniero.fulcrum.database.redis.LettuceConnection;
import java.time.Clock;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FulcrumLoquiDiscovery implements ILoquiDiscovery {

    private static final AtomicInteger THREAD_SEQUENCE = new AtomicInteger();

    private final Object lifecycleLock = new Object();
    private final LoquiDiscoveryStore store;
    private final UUID senderId;
    private final Clock clock;
    private final ScheduledExecutorService scheduler;
    private final Logger logger;
    private final Set<String> advertisements = new LinkedHashSet<>();

    private volatile LoquiDiscoveryOptions options = LoquiDiscoveryOptions.defaultOptions();
    private volatile boolean closed;
    private ScheduledFuture<?> heartbeatTask;

    public FulcrumLoquiDiscovery(LettuceConnection connection, UUID senderId, Logger logger) {
        this(
                new RedisLoquiDiscoveryStore(connection),
                senderId,
                Clock.systemUTC(),
                Executors.newSingleThreadScheduledExecutor(discoveryThreadFactory()),
                logger);
    }

    public FulcrumLoquiDiscovery(
            LoquiDiscoveryStore store, UUID senderId, Clock clock, ScheduledExecutorService scheduler, Logger logger) {
        this.store = Objects.requireNonNull(store, "Loqui discovery store can't be null");
        this.senderId = Objects.requireNonNull(senderId, "Loqui sender id can't be null");
        this.clock = Objects.requireNonNull(clock, "Loqui discovery clock can't be null");
        this.scheduler = Objects.requireNonNull(scheduler, "Loqui discovery scheduler can't be null");
        this.logger = logger == null ? Logger.getLogger(FulcrumLoquiDiscovery.class.getName()) : logger;
    }

    @Override
    public UUID getSenderId() {
        return senderId;
    }

    @Override
    public LoquiDiscoveryOptions getOptions() {
        return options;
    }

    @Override
    public void setOptions(LoquiDiscoveryOptions options) {
        if (!LoquiDiscoveryOptions.areOptionsValid(options)) {
            throw new IllegalArgumentException(
                    "Loqui discovery TTL must be at least twice a positive refresh interval");
        }

        synchronized (lifecycleLock) {
            ensureOpen();
            this.options = options;
            rescheduleHeartbeatLocked();
            refreshLocked();
        }
    }

    @Override
    public boolean advertise(String name) {
        validateName(name);

        synchronized (lifecycleLock) {
            ensureOpen();
            Optional<LoquiDiscoveryData> current = store.get(name);
            if (isLiveForeignRecord(current) && !options.aggressiveRetake()) {
                return false;
            }

            store.set(name, newHeartbeat());
            advertisements.add(name);
            ensureHeartbeatLocked();
            return true;
        }
    }

    @Override
    public boolean stopAdvertising(String name) {
        validateName(name);

        synchronized (lifecycleLock) {
            ensureOpen();
            if (!advertisements.remove(name)) {
                return false;
            }

            if (advertisements.isEmpty()) {
                cancelHeartbeatLocked();
            }

            Optional<LoquiDiscoveryData> current = store.get(name);
            if (current.isEmpty() || !senderId.equals(current.get().senderId())) {
                return false;
            }

            store.delete(name);
            return true;
        }
    }

    @Override
    public Set<String> getAdvertisements() {
        synchronized (lifecycleLock) {
            return Set.copyOf(advertisements);
        }
    }

    @Override
    public Optional<LoquiDiscoveryData> discover(String name) {
        validateName(name);
        ensureOpen();
        return store.get(name).filter(this::isLiveAndSupported);
    }

    @Override
    public Map<String, LoquiDiscoveryData> discoverAll() {
        ensureOpen();
        Map<String, LoquiDiscoveryData> live = new HashMap<>();
        store.getAll().forEach((name, data) -> {
            if (isLiveAndSupported(data)) {
                live.put(name, data);
            }
        });
        return Map.copyOf(live);
    }

    @Override
    public void close() {
        synchronized (lifecycleLock) {
            if (closed) {
                return;
            }

            closed = true;
            cancelHeartbeatLocked();
            for (String name : advertisements) {
                try {
                    Optional<LoquiDiscoveryData> current = store.get(name);
                    if (current.isPresent() && senderId.equals(current.get().senderId())) {
                        store.delete(name);
                    }
                } catch (RuntimeException e) {
                    logger.log(Level.WARNING, "Can't release Loqui discovery advertisement: " + name, e);
                }
            }
            advertisements.clear();
        }

        scheduler.shutdownNow();
    }

    private void refreshSafely() {
        synchronized (lifecycleLock) {
            if (!closed) {
                refreshLocked();
            }
        }
    }

    private void refreshLocked() {
        Set<String> lostAdvertisements = new LinkedHashSet<>();
        for (String name : advertisements) {
            try {
                Optional<LoquiDiscoveryData> current = store.get(name);
                if (isLiveForeignRecord(current) && !options.aggressiveRetake()) {
                    lostAdvertisements.add(name);
                    continue;
                }

                store.set(name, newHeartbeat());
            } catch (RuntimeException e) {
                logger.log(Level.WARNING, "Can't refresh Loqui discovery advertisement: " + name, e);
            }
        }

        advertisements.removeAll(lostAdvertisements);
        if (advertisements.isEmpty()) {
            cancelHeartbeatLocked();
        }
    }

    private LoquiDiscoveryData newHeartbeat() {
        return LoquiDiscoveryData.current(senderId, clock.millis(), options.ttl());
    }

    private boolean isLiveAndSupported(LoquiDiscoveryData data) {
        return data.version() == LoquiDiscoveryData.CURRENT_VERSION && !data.isExpired(clock.millis());
    }

    private boolean isLiveForeignRecord(Optional<LoquiDiscoveryData> current) {
        return current.isPresent()
                && !current.get().isExpired(clock.millis())
                && !senderId.equals(current.get().senderId());
    }

    private void ensureHeartbeatLocked() {
        if (heartbeatTask == null || heartbeatTask.isCancelled() || heartbeatTask.isDone()) {
            heartbeatTask = scheduler.scheduleWithFixedDelay(
                    this::refreshSafely, options.refreshInterval(), options.refreshInterval(), TimeUnit.SECONDS);
        }
    }

    private void rescheduleHeartbeatLocked() {
        cancelHeartbeatLocked();
        if (!advertisements.isEmpty()) {
            ensureHeartbeatLocked();
        }
    }

    private void cancelHeartbeatLocked() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel(false);
            heartbeatTask = null;
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Loqui discovery is closed");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Loqui discovery name can't be null or blank");
        }
    }

    private static ThreadFactory discoveryThreadFactory() {
        return runnable -> {
            Thread thread = new Thread(runnable, "fulcrum-loqui-discovery-" + THREAD_SEQUENCE.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }
}
