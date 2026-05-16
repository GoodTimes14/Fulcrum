package it.raniero.fulcrum.api.database.relational.data;

import it.raniero.fulcrum.api.database.relational.RelationalConnection;

/**
 * Provides schema initialization for relational data storage.
 */
public interface DataRepository {

    /**
     * Creates the tables required by this repository.
     */
    void createTables();

    /**
     * Gets the relational connection used by this repository.
     *
     * @return relational connection
     */
    RelationalConnection connection();
}
