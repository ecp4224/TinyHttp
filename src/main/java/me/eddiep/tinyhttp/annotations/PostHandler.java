package me.eddiep.tinyhttp.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation indicates that this method can handle POST requests with
 * the requestPath path that matches {@link PostHandler#requestPath()}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PostHandler {
    String requestPath();
}
