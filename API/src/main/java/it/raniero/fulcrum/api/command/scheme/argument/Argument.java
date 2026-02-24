package it.raniero.fulcrum.api.command.scheme.argument;

import it.raniero.fulcrum.api.command.context.ICommandContext;
import it.raniero.fulcrum.api.conversion.IConversionManager;
import it.raniero.fulcrum.api.server.FulcrumServer;

public interface Argument {

    String name();

    String description();

    boolean required();

    void compileArgument(
            int index, IConversionManager conversionManager, FulcrumServer server, ICommandContext context);

    String display();
}
