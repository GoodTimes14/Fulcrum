package it.raniero.fulcrum.api.database.loqui.message;

import java.util.Map;

public abstract class LoquiMessage {


    public LoquiMessage(Map<String, String> rawInput) {
        this.deserialize(rawInput);
    }


    public abstract void deserialize(Map<String, String> rawInput);

}
