package me.eddiep.tinyhttp.system;

import me.eddiep.tinyhttp.TinyHttpServer;

import java.util.HashMap;

public class Request {
    private Client client;
    private String requestPath;
    private HttpMethod method;
    private String httpVersion;
    private TinyHttpServer server;
    private HashMap<String, String> headers = new HashMap<String, String>();

    Request(String requestPath, HttpMethod method, String httpVersion, Client client, TinyHttpServer server) {
        this.requestPath = requestPath;
        this.method = method;
        this.httpVersion = httpVersion;
        this.client = client;
        this.server =server;
    }

    /**
     * Get the client this requestPath is for
     * @return The client this requestPath is for
     */
    public Client getClient() {
        return client;
    }

    /**
     * Get the server this requestPath came from
     * @return The server this requestPath came from
     */
    public TinyHttpServer getServer() {
        return server;
    }

    /**
     * Get the file the client is requesting. <br></br>
     * Example: If {@link Request#getRequestPath()} returns /foo/bar.txt, this method will return bar.txt. <br></br>
     * Example2: If {@link Request#getRequestPath()} returns /foo/bar%20poo, this method will return bar%20poo. <br></br>
     * @return The file this client is requesting
     */
    public String getFileRequest() {
        String[] temp = requestPath.split("/");
        return temp[temp.length - 1];
    }

    /**
     * Get the HTTP version this client supports
     * @return The HTTP version this clients supports
     */
    public String getHttpVersion() {
        return httpVersion;
    }

    /**
     * Get the {@link me.eddiep.tinyhttp.system.HttpMethod} the client requested.
     * @return The {@link me.eddiep.tinyhttp.system.HttpMethod} this client requested
     */
    public HttpMethod getRequestMethod() {
        return method;
    }

    /**
     * Get the full path the client requested
     * @return The full path the client requested
     */
    public String getRequestPath() {
        return requestPath;
    }

    void addHeader(String property, String value) {
        if (!headers.containsKey(property))
            headers.put(property, value);
    }

    /**
     * Check whether the client sent a specific header
     * @param property The property name to check for. Example: 'Host'
     * @return If the client sent the header,
     */
    public boolean hasHeader(String property) {
        return headers.containsKey(property);
    }

    /**
     * Get the value of a header the client <b>may</b> have sent. If no header is found, then a null
     * value is returned
     * @param header The property name to get the value for. Example: 'Host'
     * @return The value for that property or null if no header was found.
     */
    public String getHeaderValue(String header) {
        return headers.get(header);
    }
}
