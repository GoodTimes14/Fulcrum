package it.raniero.fulcrum.config;

import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;
import java.io.File;

public interface IFulcrumConfig {

    void initConfig(File dataFolder, String fileName, Class<? extends SettingsHolder> holder);

    <T> T get(Class<? extends SettingsHolder> holder, Property<T> property);

    <T> void set(Class<? extends SettingsHolder> holder, Property<T> property, T value);

    void saveConfig(Class<? extends SettingsHolder> holder);

    void reload();
}
