package it.raniero.fulcrum.command.scheme;

import it.raniero.fulcrum.command.context.ICommandContext;
import it.raniero.fulcrum.command.scheme.argument.Argument;
import it.raniero.fulcrum.command.scheme.argument.impl.NormalArgument;
import it.raniero.fulcrum.command.context.source.SourceType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Builder
@Getter
@Setter
@Accessors(fluent = true)
public class CommandScheme {


    private final Consumer<ICommandContext> commandExecutor;

    private final String label;

    private final String description;

    private final List<String> aliases;

    private final SourceType source;

    private final Map<String, CommandScheme> subCommands;

    private final LinkedHashMap<String, Argument> arguments;

    private CommandScheme parent;


    public static class CommandSchemeBuilder {

        private LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();

        private Map<String, CommandScheme> subCommands = new LinkedHashMap<>();

        public CommandSchemeBuilder argument(Argument argument) {

            arguments.put(argument.name(),argument);
            return this;
        }


        public CommandSchemeBuilder subCommand(CommandScheme scheme) {

            scheme.parent(scheme);
            subCommands.put(scheme.label(),scheme);
            return this;
        }
    }

}
