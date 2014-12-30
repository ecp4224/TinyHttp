package me.eddiep.tinyhttp.net;

import me.eddiep.tinyhttp.TinyHttpServer;
import me.eddiep.tinyhttp.net.http.HttpMethod;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;

public class Request {
    private Client client;
    private String requestPath;
    private HttpMethod method;
    private String httpVersion;
    private TinyHttpServer server;
    private HashMap<String, String> headers = new HashMap<String, String>();
    private InputStream contentStream;

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
     * Get the {@link me.eddiep.tinyhttp.net.http.HttpMethod} the client requested.
     * @return The {@link me.eddiep.tinyhttp.net.http.HttpMethod} this client requested
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

    void setRawContentStream(InputStream stream) {
        this.contentStream = stream;
    }

    /**
     * Get the {@link java.io.InputStream} used for reading the enclosed content of this request.
     * If no content is enclosed in this request, then this method will return null
     * @return The {@link java.io.InputStream} used for reading the enclosed content or null if no content exists
     */
    public InputStream getContentStream() {
        return contentStream;
    }

    /**
     * Read the enclosed content of this request as a {@link java.lang.String}
     * @return The enclosed content of this request, or null if no content exists
     * @throws IOException This exception is thrown if there was an error reading the content
     */
    public String getContentAsString() throws IOException {
        if (contentStream != null) {
            long length = Long.parseLong(getHeaderValue("Content-Length"));
            if (length >= Integer.MAX_VALUE) {
                Charset ASCII = Charset.forName("ASCII");
                byte[] data = new byte[server.getBufferDataLength()];
                StringBuilder builder = new StringBuilder();
                int read;
                while ((read = contentStream.read(data)) != -1) {
                    builder.append(new String(data, 0, read, ASCII));
                }
                return builder.toString();
            } else {
                byte[] data = new byte[(int)length];
                contentStream.read(data);
                return new String(data, Charset.forName("ASCII"));
            }
        }
        return null;
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
