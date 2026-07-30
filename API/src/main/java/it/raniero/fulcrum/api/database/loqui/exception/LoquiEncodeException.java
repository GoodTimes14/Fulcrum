package it.raniero.fulcrum.api.database.loqui.exception;

public class LoquiEncodeException extends RuntimeException {
    public LoquiEncodeException(String message) {
        super(message);
    }

    public LoquiEncodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
