package it.raniero.fulcrum.api.database.loqui.discovery;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Advertises and resolves named Loqui endpoints through a shared discovery store.
 */
public interface ILoquiDiscovery extends AutoCloseable {

    /**
     * Gets the sender id advertised by this discovery instance.
     *
     * @return local Loqui sender id
     */
    UUID getSenderId();

    /**
     * Sets the heartbeat, TTL, and takeover options.
     *
     * @param options discovery options
     * @throws IllegalArgumentException when the options are invalid
     */
    void setOptions(LoquiDiscoveryOptions options);

    /**
     * Gets the current discovery options.
     *
     * @return current options
     */
    LoquiDiscoveryOptions getOptions();

    /**
     * Advertises this sender under a logical name.
     *
     * @param name logical endpoint name
     * @return {@code true} when the name was acquired or already belonged to this sender
     */
    boolean advertise(String name);

    /**
     * Stops refreshing a local advertisement and deletes the stored record when it still belongs to this sender.
     *
     * @param name logical endpoint name
     * @return {@code true} when a locally owned record was deleted
     */
    boolean stopAdvertising(String name);

    /**
     * Gets the endpoint names currently refreshed by this instance.
     *
     * @return immutable snapshot of local advertisements
     */
    Set<String> getAdvertisements();

    /**
     * Resolves one live endpoint.
     *
     * @param name logical endpoint name
     * @return live, supported discovery data when available
     */
    Optional<LoquiDiscoveryData> discover(String name);

    /**
     * Resolves every live endpoint in the discovery namespace.
     *
     * @return immutable map from endpoint name to discovery data
     */
    Map<String, LoquiDiscoveryData> discoverAll();

    /** Stops heartbeats and releases locally owned advertisements. */
    @Override
    void close();
}
