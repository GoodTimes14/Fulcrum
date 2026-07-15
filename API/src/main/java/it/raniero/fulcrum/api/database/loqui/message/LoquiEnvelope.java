package it.raniero.fulcrum.api.database.loqui.message;

import it.raniero.fulcrum.api.database.loqui.exception.LoquiDecoderException;

public record LoquiEnvelope(String target, String packetId, String serializedMessage) {

    private static final String SEPARATOR = "|";

    private static final String SEPARATOR_ESC = "\\|";

    private static final String SEPARATOR_REPLACE = "xSEP_REPLACEx";

    public String serialize() {
        return target + SEPARATOR + packetId + SEPARATOR + sanitizeSeparators(serializedMessage);
    }

    public String sanitizeSeparators(String message) {
        return message.replace(SEPARATOR, SEPARATOR_REPLACE);
    }

    public static String reAddSeparators(String message) {
        return message.replace(SEPARATOR_REPLACE, SEPARATOR);
    }


    public static LoquiEnvelope deserialize(String serialized) {
        String[] array = serialized.split(SEPARATOR_ESC);
        if(array.length < 3) throw new LoquiDecoderException("Can't deserialize envelope, message too little");

        String target = array[0];
        String packetId = array[1];
        String content = reAddSeparators(array[2]);

        return new LoquiEnvelope(target, packetId, content);
    }



}
