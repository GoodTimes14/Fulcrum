package it.raniero.fulcrum.api.database.loqui.exception;

public class LoquiDecoderException extends RuntimeException {
    public LoquiDecoderException(String message) {
        super(message);
    }

    public LoquiDecoderException(String message, Throwable cause) {
        super(message, cause);
    }
}
