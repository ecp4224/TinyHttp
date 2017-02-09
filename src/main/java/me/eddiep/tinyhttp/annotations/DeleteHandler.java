package me.eddiep.tinyhttp.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation indicates that this method can handle DELETE requests with
 * the requestPath path that matches {@link DeleteHandler#requestPath()}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DeleteHandler {
    String requestPath();
}
