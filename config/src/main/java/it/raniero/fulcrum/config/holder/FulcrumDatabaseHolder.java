package it.raniero.fulcrum.config.holder;

import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.ListProperty;
import ch.jalu.configme.properties.Property;
import ch.jalu.configme.properties.types.BeanPropertyType;
import it.raniero.fulcrum.api.database.properties.ConnectionType;
import it.raniero.fulcrum.api.database.properties.DatabaseProperties;
import java.util.ArrayList;
import java.util.List;

public class FulcrumDatabaseHolder implements SettingsHolder {

    public static final Property<List<DatabaseProperties>> DATABASES;

    static {
        List<DatabaseProperties> databases = new ArrayList<>();

        databases.add(DatabaseProperties.builder()
                .connectionType(ConnectionType.SQL)
                .name("test")
                .host("localhost")
                .port(3306)
                .database("<CHANGE-ME>")
                .username("<CHANGE-ME>")
                .password("<CHANGE-ME>")
                .build());

        DATABASES = new ListProperty<>("databases", BeanPropertyType.of(DatabaseProperties.class), databases);
    }
}
