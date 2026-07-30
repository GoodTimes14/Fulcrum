package it.raniero.fulcrum.api.database.loqui.message;

import it.raniero.fulcrum.api.database.loqui.exception.LoquiDecoderException;
import it.raniero.fulcrum.api.database.loqui.exception.LoquiEncodeException;

/**
 * Wire format of a Loqui message: {@code target|from|packetId|serializedMessage}.
 *
 * @param target            receiver id, or {@code BROADCAST} for every listening instance
 * @param from              sender id
 * @param packetId          sanitized id of the message type
 * @param serializedMessage encoded message payload
 */
public record LoquiEnvelope(String target, String from, String packetId, String serializedMessage) {

    private static final String SEPARATOR = "|";

    private static final String SEPARATOR_ESC = "\\|";

    private static final int HEADER_FIELDS = 3;

    public LoquiEnvelope {
        requireHeader(target, "target");
        requireHeader(from, "from");
        requireHeader(packetId, "packetId");

        if (serializedMessage == null) {
            throw new LoquiEncodeException("Envelope serializedMessage can't be null");
        }
    }

    public String serialize() {
        return target + SEPARATOR + from + SEPARATOR + packetId + SEPARATOR + serializedMessage;
    }

    public static LoquiEnvelope deserialize(String serialized) {
        if (serialized == null) {
            throw new LoquiDecoderException("Can't deserialize a null envelope");
        }

        // The limit keeps every separator of the payload untouched and preserves an empty payload,
        // so the message never needs to be mangled on the way out.
        String[] array = serialized.split(SEPARATOR_ESC, HEADER_FIELDS + 1);
        if (array.length != HEADER_FIELDS + 1) {
            throw new LoquiDecoderException("Can't deserialize envelope, message too little");
        }

        for (int i = 0; i < HEADER_FIELDS; i++) {
            if (array[i].isEmpty()) {
                throw new LoquiDecoderException("Can't deserialize envelope, empty header field at index " + i);
            }
        }

        return new LoquiEnvelope(array[0], array[1], array[2], array[3]);
    }

    /**
     * Checks whether a value can be carried by a header field of the envelope.
     *
     * @param value value to check
     * @return {@code true} when the value is usable as a target, sender id or packet id
     */
    public static boolean isValidHeaderField(String value) {
        return value != null && !value.isEmpty() && !value.contains(SEPARATOR);
    }

    private static void requireHeader(String value, String name) {
        if (value == null || value.isEmpty()) {
            throw new LoquiEncodeException("Envelope " + name + " can't be null or empty");
        }

        if (value.contains(SEPARATOR)) {
            throw new LoquiEncodeException("Envelope " + name + " can't contain '" + SEPARATOR + "': " + value);
        }
    }
}
