package it.raniero.fulcrum.config;

import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import ch.jalu.configme.properties.Property;
import it.raniero.fulcrum.api.config.IFulcrumConfig;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FulcrumConfig implements IFulcrumConfig {

    private final Map<String, SettingsManager> managers = new HashMap<>();

    @Override
    public void reload() {
        for (SettingsManager manager : managers.values()) {
            manager.reload();
        }
    }

    public void initConfig(File dataFolder, String fileName, Class<? extends SettingsHolder> holder) {

        File configFile = new File(dataFolder, fileName);
        SettingsManager manager = SettingsManagerBuilder.withYamlFile(configFile)
                .configurationData(holder)
                .useDefaultMigrationService()
                .create();

        manager.save();
        managers.put(holder.getSimpleName(), manager);
    }

    @Override
    public void saveConfig(Class<? extends SettingsHolder> holder) {

        SettingsManager manager = managers.get(holder.getSimpleName());

        if (manager == null) {
            throw new IllegalStateException("Unbound holder (maybe forgot to load the config???)");
        }

        manager.save();
    }

    @Override
    public <T> T get(Class<? extends SettingsHolder> holder, Property<T> property) {

        SettingsManager manager = managers.get(holder.getSimpleName());

        if (manager == null) {
            throw new IllegalStateException("Unbound holder (maybe forgot to load the config???)");
        }

        return manager.getProperty(property);
    }

    @Override
    public <T> void set(Class<? extends SettingsHolder> holder, Property<T> property, T value) {

        SettingsManager manager = managers.get(holder.getSimpleName());

        if (manager == null) {
            throw new IllegalStateException("Unbound holder (maybe forgot to load the config???)");
        }

        manager.setProperty(property, value);
    }
}
