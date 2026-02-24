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

    @Comment(
            value =
                    "This is the error message displayed when a source does not have the required permission to do a certain command")
    public static final Property<String> NO_PERMISSION_SOURCE = new StringProperty(
            createPath("no", "permission", "command", "source"),
            "&cYou don't have enough permissions to execute this command!");

    @Comment(value = "This is the error displayed when a command is not compiled correctly")
    public static final Property<String> INVALID_COMMAND_ARGUMENTS =
            new StringProperty(createPath("invalid", "command", "arguments"), "&cInvalid arguments, here's the usage:");

    @Comment(value = "The default color for the command label if not specified in the scheme")
    public static final Property<String> DEFAULT_LABEL_COLOR =
            new StringProperty(createPath("default", "label", "color"), "&c");

    @Comment(value = "The default color for the subcommand label if not specified in the scheme")
    public static final Property<String> DEFAULT_SUBLABEL_COLOR =
            new StringProperty(createPath("default", "subcommand", "label", "color"), "&7");

    @Comment(value = "The default color for the argument if not specified in the scheme")
    public static final Property<String> DEFAULT_ARGUMENT_COLOR =
            new StringProperty(createPath("default", "argument", "color"), "&f");

    @Comment(value = "The default color for the argument hover if not specified in the scheme")
    public static final Property<String> DEFAULT_ARGUMENT_HOVER_COLOR =
            new StringProperty(createPath("default", "argument", "hover", "color"), "&c");

    @Comment(value = "The default color for the description if not specified in the scheme")
    public static final Property<String> DEFAULT_DESCRIPTION_COLOR =
            new StringProperty(createPath("default", "description", "color"), "&c");

    @Comment(value = "The message that should be showed every time a command usage is shown")
    public static final Property<String> COMMAND_HELP_PREAMBLE = new StringProperty(
            createPath("command", "help", "preamble"),
            "%argument_color%Showing help for command: %label_color%%label%");
}
