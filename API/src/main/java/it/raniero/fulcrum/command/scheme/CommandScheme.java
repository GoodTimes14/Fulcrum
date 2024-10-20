package it.raniero.fulcrum.command.scheme;

import it.raniero.fulcrum.command.context.ICommandContext;
import it.raniero.fulcrum.command.scheme.arguments.CommandArgument;
import it.raniero.fulcrum.command.scheme.enums.SourceType;
import lombok.Builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Builder
public record CommandScheme(Consumer<ICommandContext<?>> commandExecutor,
                            String label,
                            List<String> aliases,
                            SourceType source,
                            Map<String, CommandArgument> arguments) {


    public static class CommandSchemeBuilder {

        private Map<String, CommandArgument> arguments;

        public CommandSchemeBuilder argument(CommandArgument argument) {

            if(arguments != null) {
                arguments.put(argument.name(),argument);
            } else {
                arguments = new HashMap<>();
            }
            return this;
        }

    }

}
