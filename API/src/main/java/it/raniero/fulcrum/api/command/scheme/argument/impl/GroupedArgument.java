package it.raniero.fulcrum.api.command.scheme.argument.impl;

import it.raniero.fulcrum.api.command.context.ICommandContext;
import it.raniero.fulcrum.api.command.context.result.ContextResult;
import it.raniero.fulcrum.api.command.scheme.argument.Argument;
import it.raniero.fulcrum.api.conversion.IConversionManager;
import it.raniero.fulcrum.api.server.FulcrumServer;
import java.util.Arrays;
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
            context.result(ContextResult.INVALID_ARGUMENTS);
        } else {
            context.addArgument(result);
        }
    }

    @Override
    public String display() {
        return "["
                + String.join(",", Arrays.stream(values).map(Object::toString).toList()) + "]";
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
