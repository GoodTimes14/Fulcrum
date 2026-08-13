package it.raniero.fulcrum.api.database.loqui.exception;

/** Raised when a Loqui discovery operation cannot be completed. */
public class LoquiDiscoveryException extends RuntimeException {

    public LoquiDiscoveryException(String message) {
        super(message);
    }

    public LoquiDiscoveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
