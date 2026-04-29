package it.raniero.fulcrum.api.database.redis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a listener method as a Redis channel subscriber.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Listen {

    /**
     * Gets the Redis channel handled by the annotated method.
     *
     * @return Redis channel name
     */
    String channel();
}
