package it.raniero.fulcrum.api.config;

import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;
import java.io.File;

/**
 * Provides access to Fulcrum configuration files backed by ConfigMe holders.
 */
public interface IFulcrumConfig {

    /**
     * Initializes a configuration file for the given settings holder.
     *
     * @param dataFolder folder where the configuration file is stored
     * @param fileName configuration file name
     * @param holder settings holder class that declares the properties
     */
    void initConfig(File dataFolder, String fileName, Class<? extends SettingsHolder> holder);

    /**
     * Reads a property value from the configuration associated with the holder.
     *
     * @param holder settings holder class that owns the property
     * @param property property to read
     * @param <T> property value type
     * @return configured value
     */
    <T> T get(Class<? extends SettingsHolder> holder, Property<T> property);

    /**
     * Updates a property value in the configuration associated with the holder.
     *
     * @param holder settings holder class that owns the property
     * @param property property to update
     * @param value new property value
     * @param <T> property value type
     */
    <T> void set(Class<? extends SettingsHolder> holder, Property<T> property, T value);

    /**
     * Saves the configuration associated with the holder.
     *
     * @param holder settings holder class whose configuration should be saved
     */
    void saveConfig(Class<? extends SettingsHolder> holder);

    /**
     * Reloads all managed configuration values from disk.
     */
    void reload();
}
