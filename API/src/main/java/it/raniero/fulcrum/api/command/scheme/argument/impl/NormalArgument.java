package it.raniero.fulcrum.api.command.scheme.argument.impl;

import it.raniero.fulcrum.api.command.context.ICommandContext;
import it.raniero.fulcrum.api.command.context.result.ContextResult;
import it.raniero.fulcrum.api.command.scheme.argument.Argument;
import it.raniero.fulcrum.api.conversion.IConversionManager;
import it.raniero.fulcrum.api.server.FulcrumServer;
import java.util.Arrays;
import lombok.Builder;

@Builder
public record NormalArgument(
        String name, Class<?> type, boolean required, boolean suggestPlayersInTab, String description)
        implements Argument {

    @Override
    public void compileArgument(
            int index, IConversionManager conversionManager, FulcrumServer server, ICommandContext context) {
        if (type.isAssignableFrom(String[].class)) {

            String[] copy =
                    Arrays.copyOfRange(context.originalParameters(), index, context.originalParameters().length);
            context.addArgument(copy);
        } else {

            if (type == server.getPlayerClass()) {}

            String parameter = context.originalParameters()[index];

            boolean result = conversionManager.convertAndAddArgument(type, parameter, context);
            if (!result) {
                context.setResult(ContextResult.INVALID_ARGUMENTS);
            }
        }
    }

    @Override
    public String display() {
        return "<" + name + ">";
    }
}
