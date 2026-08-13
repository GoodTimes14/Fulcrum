package it.raniero.fulcrum.api.database.loqui.discovery;

import java.util.Map;
import java.util.Optional;

/**
 * Storage contract used by Loqui discovery implementations.
 *
 * <p>The contract deliberately consists of basic set, get, list, and delete operations so storage adapters do not
 * impose coordination mechanisms on discovery policy.
 */
public interface LoquiDiscoveryStore {

    /**
     * Stores or replaces a named discovery record and refreshes its storage expiration.
     *
     * <p>The record must be removed by the backing store after {@link LoquiDiscoveryData#updateTTL()} seconds unless
     * another write refreshes it.
     *
     * @param name logical endpoint name
     * @param data discovery record
     */
    void set(String name, LoquiDiscoveryData data);

    /**
     * Gets a named discovery record.
     *
     * @param name logical endpoint name
     * @return stored record when present and readable
     */
    Optional<LoquiDiscoveryData> get(String name);

    /**
     * Gets all readable records in the discovery namespace.
     *
     * @return records keyed by logical endpoint name
     */
    Map<String, LoquiDiscoveryData> getAll();

    /**
     * Deletes a named record.
     *
     * @param name logical endpoint name
     */
    void delete(String name);
}
