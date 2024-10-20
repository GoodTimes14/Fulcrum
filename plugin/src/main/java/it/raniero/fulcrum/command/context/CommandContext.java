package it.raniero.fulcrum.command.context;

import lombok.AllArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Setter
public class CommandContext<S> implements ICommandContext<S> {

    private S sender;

    private List<Object> arguments;


    @Override
    public S sender() {
        return sender;
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
}
