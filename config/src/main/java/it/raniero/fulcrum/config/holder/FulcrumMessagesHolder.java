package it.raniero.fulcrum.config.holder;

import static it.raniero.fulcrum.config.utils.ConfigUtils.createPath;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;
import ch.jalu.configme.properties.StringProperty;

public class FulcrumMessagesHolder implements SettingsHolder {

    @Comment(
            value =
                    "This is the error displayed when an unexpected source tries to execute a command (console or player only command)")
    public static final Property<String> INVALID_COMMAND_SOURCE =
            new StringProperty(createPath("invalid", "command", "source"), "&cInvalid command source");

    @Comment(value = "This is the error displayed when a command is not compiled correctly")
    public static final Property<String> INVALID_COMMAND_ARGUMENTS =
            new StringProperty(createPath("invalid", "command", "arguments"), "&cInvalid arguments, here's the usage:");
}
