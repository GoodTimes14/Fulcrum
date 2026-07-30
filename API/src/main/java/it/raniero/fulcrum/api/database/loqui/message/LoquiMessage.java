package it.raniero.fulcrum.api.database.loqui.message;

/**
 * Base class of every message exchanged through Loqui.
 */
public abstract class LoquiMessage {

    /**
     * Builds an empty message, meant to be filled by the subclass before sending it.
     */
    protected LoquiMessage() {}

    /**
     * Reads the message state back from the raw fields of a received payload.
     *
     * @param content Wrapper for the raw received payload
     */
    public abstract void deserialize(LoquiContent content);
}
