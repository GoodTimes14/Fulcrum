package it.raniero.fulcrum.command.scheme.argument.impl;

import it.raniero.fulcrum.command.context.ICommandContext;
import it.raniero.fulcrum.command.context.result.ContextResult;
import it.raniero.fulcrum.command.scheme.argument.Argument;
import it.raniero.fulcrum.conversion.IConversionManager;
import it.raniero.fulcrum.conversion.IConverter;
import it.raniero.fulcrum.server.FulcrumServer;
import lombok.Builder;

import java.util.Arrays;

@Builder
public record NormalArgument(String name,
                             Class<?> type,
                             boolean required,
                             String description) implements Argument {



    @Override
    public void compileArgument(int index, IConversionManager conversionManager, FulcrumServer server, ICommandContext context) {
        if(type.isAssignableFrom(String[].class)) {

            String[] copy = Arrays.copyOfRange(context.originalParameters(),index,context.originalParameters().length);
            context.addArgument(copy);
        } else {

            String parameter = context.originalParameters()[index];

            boolean result  = conversionManager.convertAndAddArgument(type,parameter,context);
            if (!result) {
                context.setResult(ContextResult.INVALID_ARGUMENTS);
            }
        }
    }
}
