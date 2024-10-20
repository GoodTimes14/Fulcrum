package it.raniero.fulcrum.command.context;

import java.util.Optional;

public interface ICommandContext<S> {

    S sender();

    <T> Optional<T> argument(int index, Class<T> type);

}

