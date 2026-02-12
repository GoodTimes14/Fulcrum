package it.raniero.fulcrum.command.context;

import it.raniero.fulcrum.api.command.context.ICommandContext;
import it.raniero.fulcrum.api.command.context.result.ContextResult;
import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Setter
public class CommandContext implements ICommandContext {

    private final FulcrumSource source;

    private final List<Object> arguments;

    private final String[] originalParameters;

    private ContextResult result = ContextResult.OK;

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
        if (index >= arguments.size()) {
            return Optional.empty();
        }

        Object argument = arguments.get(index);

        if (argument == null) {
            return Optional.empty();
        }

        try {

            T castArgument = type.cast(argument);

            return Optional.of(castArgument);

        } catch (ClassCastException e) {
            throw new IllegalArgumentException("type " + type.getSimpleName()
                    + " is not assignable to: "
                    + argument.getClass().getSimpleName());
        }
    }

    @Override
    public void addArgument(Object object) {
        if (object == null) return;

        arguments.add(object);
    }

    @Override
    public ContextResult result() {
        return result;
    }
}
