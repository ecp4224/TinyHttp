package me.eddiep.tinyhttp.net;

import me.eddiep.tinyhttp.net.http.StatusCode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Response {
    StreamResponse streamResponse;

    private String content = "";
    byte[] rawContents;
    private Client client;
    private StatusCode statusCode = StatusCode.OK;
    private HashMap<String, String> headers = new HashMap<String, String>();

    Response(Client client) {
        this.client = client;
    }

    Response(Response response) {
        this.content = response.content;
        this.rawContents = response.rawContents;
        this.client = response.client;
        this.statusCode = response.statusCode;
        this.headers = response.headers;
    }

    /**
     * Get the client this response is for
     * @return The client this response is for
     */
    public Client getClient() {
        return client;
    }

    /**
     * Set the status code for this response
     * @param code The {@link me.eddiep.tinyhttp.net.http.StatusCode}
     */
    public void setStatusCode(StatusCode code) {
        this.statusCode = code;
    }

    /**
     * Get the content type of this response.
     * @return The content type of this response as a {@link java.lang.String}
     */
    public String getContentType() {
        if (!hasHeader("Content-Type"))
            addHeader("Content-Type", "text/html");

        return headers.get("Content-Type");
    }

    /**
     * Create a new {@link me.eddiep.tinyhttp.net.StreamResponse} object. <br></br>
     * A stream response allows you to stream large data in real time to the client as a response. You can begin to write
     * data by setting the contentLength using {@link me.eddiep.tinyhttp.net.StreamResponse#setContentLength(long)} and then by
     * calling {@link StreamResponse#startStream()} and writing to the returned {@link java.io.OutputStream}
     *
     * @see StreamResponse#startStream()
     * @return A new {@link me.eddiep.tinyhttp.net.StreamResponse} object
     */
    public StreamResponse createStreamResponse() {
        if (streamResponse != null)
            throw new IllegalStateException("A StreamResponse object has already been created for this response!");

        streamResponse = new StreamResponse(this);

        return streamResponse;
    }

    /**
     * Create a new {@link me.eddiep.tinyhttp.net.StreamResponse} object with the specified contentLength. <br></br>
     * A stream response allows you to stream large data in real time to the client as a response. You can begin to write
     * data by calling {@link StreamResponse#startStream()} and writing to the returned {@link java.io.OutputStream}
     *
     * @see StreamResponse#startStream()
     * @return A new {@link me.eddiep.tinyhttp.net.StreamResponse} object
     */
    public StreamResponse createStreamResponse(long contentLength) {
        StreamResponse r = createStreamResponse();
        r.setContentLength(contentLength);

        return r;
    }

    /**
     * Set the content type of this response.
     * @param type The content type of this response as a {@link java.lang.String}
     */
    public void setContentType(String type) {
        addHeader("Content-Type", type);
    }

    /**
     * Get the current status code for this response
     * @return The {@link me.eddiep.tinyhttp.net.http.StatusCode} set for this response
     */
    public StatusCode getStatusCode() {
        return statusCode;
    }

    /**
     * Add a header to this response
     * @param property The property name of this header
     * @param value The value of this header
     */
    public void addHeader(String property, String value) {
        this.headers.put(property, value);
    }

    /**
     * Remove a header to this response
     * @param property The property name of the header to remove
     */
    public void removeHeader(String property) {
        this.headers.remove(property);
    }

    /**
     * Check to see whether or not this response has a header
     * @param property The property name of the header to check for
     * @return Will return true if this response has a header with that property name, otherwise false
     */
    public boolean hasHeader(String property) {
        return headers.containsKey(property);
    }

    /**
     * Get all headers in an unmodifiable {@link java.util.Map}
     *
     * @see Collections#unmodifiableMap(java.util.Map)
     * @return All headers in this response
     */
    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    /**
     * Get the current response content in this response
     * @return The content as a string
     */
    public String getContent() {
        return content;
    }

    /**
     * Set the raw content of this response. The raw content is automatically generated after the request has been processed, but you
     * can prevent this by setting your own raw content. <b>Setting a raw content will ignore {@link Response#getContent()}</b>
     * @param data The raw content to respond with.
     */
    public void setRawContent(byte[] data) {
        this.rawContents = data;
    }

    /**
     * Set the raw response content of this response. Any echo's made will not be preserved. This is a good place
     * to set HTML from a file
     * @param content The content to set as a string
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Append to the current content of this response.
     * @param string The content to append as a string
     */
    public void echo(String string) {
        content += string;
    }

    /**
     * Append to the current content of this response.
     * @param i The content to append as an int
     */

    public void echo(int i)  {
        echo("" + i);
    }

    /**
     * Append to the current content of this response.
     * @param f The content to append as a float
     */
    public void echo(float f) {
        echo("" + f);
    }

    /**
     * Append to the current content of this response.
     * @param l The content to append as a long
     */
    public void echo(long l) {
        echo("" + l);
    }

    /**
     * Append to the current content of this response.
     * @param bool The content to append as a bool
     */
    public void echo(boolean bool) {
        echo("" + bool);
    }

    /**
     * Append to the current content of this response.
     * @param obj The content to append as an object
     */
    public void echo(Object obj) {
        echo(obj.toString());
    }
}
