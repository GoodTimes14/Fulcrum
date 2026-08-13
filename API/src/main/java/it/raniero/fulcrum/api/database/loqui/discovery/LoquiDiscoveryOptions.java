package it.raniero.fulcrum.api.database.loqui.discovery;

/**
 * Timing and conflict options for Loqui discovery leases.
 *
 * @param refreshInterval seconds between advertisement heartbeats
 * @param ttl seconds before an advertisement without a heartbeat expires
 * @param aggressiveRetake whether an advertiser may replace a live lease owned by another sender
 */
public record LoquiDiscoveryOptions(long refreshInterval, long ttl, boolean aggressiveRetake) {

    public static final long DEFAULT_REFRESH_INTERVAL = 15;
    public static final long DEFAULT_TTL = 45;

    /**
     * Gets conservative default lease options.
     *
     * @return a 15-second heartbeat and 45-second TTL without forced takeover
     */
    public static LoquiDiscoveryOptions defaultOptions() {
        return new LoquiDiscoveryOptions(DEFAULT_REFRESH_INTERVAL, DEFAULT_TTL, false);
    }

    /**
     * Checks that timing values are positive and leave room for at least one missed heartbeat.
     *
     * @param options options to validate
     * @return {@code true} when the TTL is at least twice the refresh interval
     */
    public static boolean areOptionsValid(LoquiDiscoveryOptions options) {
        return options != null
                && options.refreshInterval() > 0
                && options.ttl() > 0
                && options.refreshInterval() <= options.ttl() / 2;
    }
}
