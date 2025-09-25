package it.raniero.fulcrum.command.exception;

import it.raniero.fulcrum.command.IFulcrumCommand;
import lombok.Getter;

@Getter
public class FulcrumCommandException extends RuntimeException {

    private final IFulcrumCommand command;

    public FulcrumCommandException(IFulcrumCommand command, String message) {
        super(message);
        this.command = command;
    }
}
