package me.eddiep.tinyhttp.system;

public enum HttpMethod {
    GET,
    PUT,
    POST,
    DELETE,
    UNKNOWN;

    public static HttpMethod toHttpMethod(String raw) {
        for (HttpMethod m : HttpMethod.values()) {
            if (m.name().toLowerCase().equals(raw.toLowerCase()))
                return m;
        }
        return UNKNOWN;
    }
}
