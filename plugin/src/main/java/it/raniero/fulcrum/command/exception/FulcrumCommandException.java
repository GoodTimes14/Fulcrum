package it.raniero.fulcrum.command.exception;

import it.raniero.fulcrum.command.FulcrumCommand;
import lombok.Getter;

@Getter
public class FulcrumCommandException extends RuntimeException {

    private final FulcrumCommand command;

    public FulcrumCommandException(FulcrumCommand command, String message) {
        super(message);
        this.command = command;
    }

}
