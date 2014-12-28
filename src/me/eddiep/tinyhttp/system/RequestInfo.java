package me.eddiep.tinyhttp.system;

import me.eddiep.tinyhttp.TinyHttpServer;

import java.util.HashMap;

public class RequestInfo {
    private Client client;
    private String requestPath;
    private HttpMethod method;
    private String httpVersion;
    private TinyHttpServer server;
    private HashMap<String, String> headers = new HashMap<String, String>();

    public RequestInfo(String requestPath, HttpMethod method, String httpVersion, Client client, TinyHttpServer server) {
        this.requestPath = requestPath;
        this.method = method;
        this.httpVersion = httpVersion;
        this.client = client;
        this.server =server;
    }

    public Client getClient() {
        return client;
    }

    public TinyHttpServer getServer() {
        return server;
    }

    public String getFileRequest() {
        String[] temp = requestPath.split("/");
        return temp[temp.length - 1];
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public HttpMethod getRequestMethod() {
        return method;
    }

    public String getRequestPath() {
        return requestPath;
    }

    void addHeader(String property, String value) {
        if (!headers.containsKey(property))
            headers.put(property, value);
    }

    public boolean hasHeader(String property) {
        return headers.containsKey(property);
    }

    public String getHeaderValue(String header) {
        return headers.get(header);
    }
}
