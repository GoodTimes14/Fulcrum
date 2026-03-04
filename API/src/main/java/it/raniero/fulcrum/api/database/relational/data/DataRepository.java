package it.raniero.fulcrum.api.database.relational.data;

import it.raniero.fulcrum.api.database.relational.RelationalConnection;

public interface DataRepository {

    void createTables();

    RelationalConnection connection();
}
