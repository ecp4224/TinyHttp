package me.eddiep.tinyhttp.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation indicates that this method can handle PUT requests with
 * the requestPath path that matches {@link PutHandler#requestPath()}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PutHandler {
    String requestPath();
}
