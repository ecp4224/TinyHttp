package me.eddiep.tinyhttp.net.http;

/**
 * Server status codes that can be responded with.
 */
public enum StatusCode {
    Continue(100),
    OK(200),
    Created(201),
    Accepted(202),
    NoContent(204, "No Content"),
    ResetContent(205, "Reset Content"),
    PartialContent(206, "Partial Content"),
    MovedPermanently(301, "Moved Permanently"),
    Found(302),
    NotModified(304, "Not Modified"),
    TemporaryRedirect(307, "Temporary Redirect"),
    BadRequest(400, "Bad Request"),
    Unauthorized(401),
    Forbidden(403),
    NotFound(404, "Not Found"),
    MethodNotAllowed(405, "Method Not Allowed"),
    NotAcceptable(406, "Not Acceptable"),
    RequestTimeout(408, "Request Timeout"),
    Conflict(409),
    Gone(410),
    LengthRequired(411, "Length Required"),
    PreconditionFailed(412, "Precondition Failed"),
    RequestEntityTooLarge(413, "Request Entity Too Large"),
    RequestURITooLong(414, "Request-URI Too Long"),
    UnsupportedMediaType(415, "Unsupported Media Type"),
    RequestedRangeNotSatisfiable(416, "Requested Range Not Satisfiable"),
    ExpectationFailed(417, "Expectation Failed"),
    InternalServerError(500, "Internal Server Error"),
    NotImplemented(501, "Not Implemented"),
    BadGateway(502, "Bad Gateway"),
    ServiceUnavailable(503, "Service Unavailable"),
    GatewayTimeout(504, "Gateway Timeout"),
    HTTPVersionNotSupported(505, "HTTP Version Not Supported");

    int code;
    String name;
    StatusCode(int code) {
        this.code = code;
        this.name = name();
    }
    StatusCode(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
