package it.raniero.fulcrum.database.redis.utils;

import it.raniero.fulcrum.database.redis.Listen;
import it.raniero.fulcrum.database.redis.RedisListener;
import java.lang.reflect.Method;
import lombok.Data;

@Data
public class RedisMethod {

    private final RedisListener holder;
    private final Listen annotation;
    private final Method method;
}
