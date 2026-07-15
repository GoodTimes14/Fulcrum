package it.raniero.fulcrum.api.database.loqui;

import io.lettuce.core.RedisFuture;
import it.raniero.fulcrum.api.database.loqui.message.LoquiEnvelope;
import it.raniero.fulcrum.api.database.loqui.message.LoquiMessage;

import java.util.Map;
import java.util.function.Function;

public interface ILoqui {


    void registerMessageDecoder(Class<? extends LoquiMessage> messageClass, Function<Map<String, String>, LoquiMessage> decoder);

    void registerChannel(String channel);

    LoquiMessage decodeMessage(LoquiEnvelope envelope);

    void sendMessage(String channel, String target, LoquiMessage message);

    RedisFuture<Long> sendMessageAsync(String channel, String target, LoquiMessage message);

}
