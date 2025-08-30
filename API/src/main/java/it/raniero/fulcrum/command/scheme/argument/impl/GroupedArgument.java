package it.raniero.fulcrum.command.scheme.argument.impl;

import it.raniero.fulcrum.command.context.ICommandContext;
import it.raniero.fulcrum.command.scheme.argument.Argument;
import it.raniero.fulcrum.conversion.IConversionManager;
import it.raniero.fulcrum.server.FulcrumServer;
import lombok.Builder;

@Builder
public record GroupedArgument(String name, Class<?> type, boolean required, String description, Object... values)
        implements Argument {

    @Override
    public void compileArgument(
            int index, IConversionManager conversionManager, FulcrumServer server, ICommandContext context) {}
}
