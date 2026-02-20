package it.raniero.fulcrum.api.command.scheme;

import it.raniero.fulcrum.api.command.context.ICommandContext;
import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.command.context.source.SourceType;
import it.raniero.fulcrum.api.command.scheme.argument.Argument;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Builder
@Getter
@Setter
@Accessors(fluent = true)
public class CommandScheme {

    private final Consumer<ICommandContext> commandExecutor;

    private String label;

    private final String description;

    private List<String> aliases;

    private final SourceType source;

    private final Map<String, CommandScheme> subCommands;

    private final LinkedHashMap<String, Argument> arguments;

    private final String permission;

    private final String labelColor;

    private final String argumentColor;

    private final String argumentHoverColor;

    private final String descriptionColor;

    private CommandScheme parent;

    public static class CommandSchemeBuilder {

        private LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();

        private Map<String, CommandScheme> subCommands = new LinkedHashMap<>();

        public CommandSchemeBuilder argument(Argument argument) {

            arguments.put(argument.name(), argument);
            return this;
        }

        public CommandSchemeBuilder subCommand(CommandScheme scheme) {

            subCommands.put(scheme.label(), scheme);
            return this;
        }
    }

    public void parentizeSubCommands() {
        for (CommandScheme subCommand : subCommands.values()) {
            subCommand.parent(this);
            subCommand.parentizeSubCommands();
        }
    }

    public boolean checkPermission(FulcrumSource source) {
        return permission == null || source.hasPermission(permission);
    }
}
