package it.raniero.fulcrum.api.database.loqui.discovery;

import java.util.Objects;
import java.util.UUID;

/**
 * A live Loqui endpoint advertised through discovery.
 *
 * @param version protocol version used to encode the record
 * @param senderId Loqui sender id that can be used as a message target
 * @param lastUpdate epoch time in milliseconds of the last successful heartbeat
 * @param updateTTL number of seconds for which the heartbeat is valid
 */
public record LoquiDiscoveryData(int version, UUID senderId, long lastUpdate, long updateTTL) {

    /** Current discovery record protocol version. */
    public static final int CURRENT_VERSION = 1;

    private static final String SEPARATOR = "|";

    public LoquiDiscoveryData {
        if (version <= 0) {
            throw new IllegalArgumentException("Discovery version must be positive");
        }
        Objects.requireNonNull(senderId, "Discovery sender id can't be null");
        if (lastUpdate < 0) {
            throw new IllegalArgumentException("Discovery last update can't be negative");
        }
        if (updateTTL <= 0) {
            throw new IllegalArgumentException("Discovery update TTL must be positive");
        }
    }

    /**
     * Creates a record using the currently supported protocol version.
     *
     * @param senderId Loqui sender id
     * @param lastUpdate heartbeat time in epoch milliseconds
     * @param updateTTL heartbeat TTL in seconds
     * @return current-version discovery record
     */
    public static LoquiDiscoveryData current(UUID senderId, long lastUpdate, long updateTTL) {
        return new LoquiDiscoveryData(CURRENT_VERSION, senderId, lastUpdate, updateTTL);
    }

    /** @return discovery record protocol version */
    public int getVersion() {
        return version;
    }

    /** @return Loqui sender id */
    public UUID getSenderId() {
        return senderId;
    }

    /** @return last heartbeat in epoch milliseconds */
    public long getLastUpdate() {
        return lastUpdate;
    }

    /** @return heartbeat TTL in seconds */
    public long getUpdateTTL() {
        return updateTTL;
    }

    /**
     * Serializes this record for a string-backed discovery store.
     *
     * @return version, sender, heartbeat, and TTL fields separated by {@code |}
     */
    public String serialize() {
        return version + SEPARATOR + senderId + SEPARATOR + lastUpdate + SEPARATOR + updateTTL;
    }

    /**
     * Rebuilds discovery data from {@link #serialize()} output.
     *
     * @param serialized serialized discovery record
     * @return decoded discovery data
     * @throws IllegalArgumentException when the input is malformed
     */
    public static LoquiDiscoveryData deserialize(String serialized) {
        if (serialized == null || serialized.isBlank()) {
            throw new IllegalArgumentException("Serialized discovery data can't be null or blank");
        }

        String[] fields = serialized.split("\\|", -1);
        if (fields.length != 4) {
            throw new IllegalArgumentException("Serialized discovery data must contain four fields");
        }

        try {
            return new LoquiDiscoveryData(
                    Integer.parseInt(fields[0]),
                    UUID.fromString(fields[1]),
                    Long.parseLong(fields[2]),
                    Long.parseLong(fields[3]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Serialized discovery data contains an invalid number", e);
        }
    }

    /**
     * Gets the epoch time in milliseconds at which this record becomes stale.
     *
     * @return expiration epoch time, saturated at {@link Long#MAX_VALUE}
     */
    public long expiresAt() {
        try {
            return Math.addExact(lastUpdate, Math.multiplyExact(updateTTL, 1_000L));
        } catch (ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
    }

    /**
     * Checks whether this heartbeat is stale at the supplied time.
     *
     * @param currentTime epoch time in milliseconds
     * @return {@code true} once the heartbeat TTL has elapsed
     */
    public boolean isExpired(long currentTime) {
        return currentTime >= expiresAt();
    }
}
