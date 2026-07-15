package it.raniero.fulcrum.api.database.loqui.message;

public record LoquiEnvelope(String target, String packetId, String serializedMessage) {

    private static final String SEPARATOR = "|";

    private static final String SEPARATOR_REPLACE = "xSEP_REPLACEx";

    public String serialize() {
        return target + SEPARATOR + sanitizeSeparators(serializedMessage);
    }

    public String sanitizeSeparators(String message) {
        return message.replace(SEPARATOR, SEPARATOR_REPLACE);
    }

    public String reAddSeparators(String message) {
        return message.replace(SEPARATOR_REPLACE, SEPARATOR);
    }




}
