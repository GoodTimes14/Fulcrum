package it.raniero.fulcrum.command.scheme.argument.impl;

import it.raniero.fulcrum.command.context.ICommandContext;
import it.raniero.fulcrum.command.context.result.ContextResult;
import it.raniero.fulcrum.command.scheme.argument.Argument;
import it.raniero.fulcrum.conversion.IConversionManager;
import it.raniero.fulcrum.server.FulcrumServer;
import lombok.Builder;

@Builder
public record GroupedArgument(String name, Class<?> type, boolean required, String description, Object... values)
        implements Argument {

    @Override
    public void compileArgument(
            int index, IConversionManager conversionManager, FulcrumServer server, ICommandContext context) {
        String parameter = context.originalParameters()[index];

        Object result = conversionManager.convertArgument(type, parameter);
        if (result == null || !isValueIncluded(result)) {
            context.setResult(ContextResult.INVALID_ARGUMENTS);
        } else {
            context.addArgument(result);
        }
    }

    private boolean isValueIncluded(Object input) {
        for (Object value : values) {
            if (input.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
