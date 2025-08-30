package it.raniero.fulcrum.command.scheme.argument;

import it.raniero.fulcrum.command.context.ICommandContext;
import it.raniero.fulcrum.conversion.IConversionManager;
import it.raniero.fulcrum.server.FulcrumServer;

public interface Argument {

    String name();

    String description();

    boolean required();

    void compileArgument(
            int index, IConversionManager conversionManager, FulcrumServer server, ICommandContext context);
}
