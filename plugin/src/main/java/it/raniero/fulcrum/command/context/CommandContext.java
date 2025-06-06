package it.raniero.fulcrum.command.context;

import it.raniero.fulcrum.command.context.result.ContextResult;
import it.raniero.fulcrum.command.context.source.FulcrumSource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Setter
public class CommandContext implements ICommandContext {

    private final FulcrumSource source;

    private final List<Object> arguments;

    private final String[] originalParameters;

    private ContextResult result;


    @Override
    public FulcrumSource source() {
        return source;
    }

    @Override
    public String[] originalParameters() {
        return originalParameters;
    }


    @Override
    public <T> Optional<T> argument(int index, Class<T> type) {

        Object argument = arguments.get(index);

        if (argument == null) {
            return Optional.empty();
        }

        try {

            T castArgument =  type.cast(argument);

            return Optional.of(castArgument);

        } catch (ClassCastException e) {
            throw new IllegalArgumentException("type " + type.getSimpleName()
                    + " is not assignable to: "
                    + argument.getClass().getSimpleName());
        }
    }

    @Override
    public void addArgument(Object object) {
        arguments.add(object);
    }

    @Override
    public ContextResult result() {
        return result;
    }


}
