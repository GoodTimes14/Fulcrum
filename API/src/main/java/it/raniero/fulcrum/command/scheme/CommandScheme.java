package it.raniero.fulcrum.command.scheme;

import it.raniero.fulcrum.command.context.ICommandContext;
import it.raniero.fulcrum.command.scheme.argument.Argument;
import it.raniero.fulcrum.command.scheme.argument.impl.NormalArgument;
import it.raniero.fulcrum.command.context.source.SourceType;
import lombok.Builder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Builder
public record CommandScheme(Consumer<ICommandContext> commandExecutor,
                            String label,
                            List<String> aliases,
                            SourceType source,
                            Map<String, CommandScheme> subCommands,
                            LinkedHashMap<String, Argument> arguments) {


    public static class CommandSchemeBuilder {

        private LinkedHashMap<String, NormalArgument> arguments = new LinkedHashMap<>();
        private Map<String, CommandScheme> subCommands = new LinkedHashMap<>();

        public CommandSchemeBuilder argument(NormalArgument argument) {

            arguments.put(argument.name(),argument);
            return this;
        }


        public CommandSchemeBuilder subCommand(String name,CommandScheme scheme) {

            subCommands.put(scheme.label(),scheme);
            return this;
        }
    }

}
