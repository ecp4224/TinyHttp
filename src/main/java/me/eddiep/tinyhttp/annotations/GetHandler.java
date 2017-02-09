package me.eddiep.tinyhttp.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation indicates that this method can handle GET requests with
 * the requestPath path that matches {@link GetHandler#requestPath()}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GetHandler {
    String requestPath();
}
