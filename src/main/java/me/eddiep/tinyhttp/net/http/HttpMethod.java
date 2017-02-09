package me.eddiep.tinyhttp.net.http;

/**
 * HTTP methods this tinyhttp server can handle
 */
public enum HttpMethod {
    /**
     * A GET requestPath
     */
    GET,
    /**
     * A PUT requestPath
     */
    PUT,
    /**
     * A POST requestPath
     */
    POST,
    /**
     * A DELETE requestPath
     */
    DELETE,
    /**
     * This indicates that the requestPath sent is unknown
     */
    UNKNOWN;

    public static HttpMethod toHttpMethod(String raw) {
        for (HttpMethod m : HttpMethod.values()) {
            if (m.name().toLowerCase().equals(raw.toLowerCase()))
                return m;
        }
        return UNKNOWN;
    }
}
