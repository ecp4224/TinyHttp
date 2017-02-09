package me.eddiep.tinyhttp.net;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * A StreamResponse allows you to stream the content of your response to the client. This is useful for sending large
 * files or data to the client.
 *
 * <b>You must set the content length of the data before invoking {@link StreamResponse#startStream()}</b>
 */
public class StreamResponse extends Response {
    private long contentLength;
    private OutputStream out;

    StreamResponse(Response clone) {
        super(clone);
    }

    /**
     * Get the content length of this response
     * @return The content length
     */
    public long getContentLength() {
        return contentLength;
    }

    /**
     * Set the content length of this response
     * @param l The content length of this response
     */
    public void setContentLength(long l) {
        this.contentLength = l;
    }

    /**
     * Start streaming the content to the client. This method will create a {@link java.io.OutputStream} object and
     * write the appropriate HTTP headers to the stream before returning the {@link java.io.OutputStream} object
     * @return A stream you can write your data to
     * @throws IOException If there was an error creating or writing to the stream
     * @throws java.lang.IllegalStateException If no content length was specified for this response or if the stream was already created
     */
    public OutputStream startStream() throws IOException {
        if (contentLength == 0)
            throw new IllegalStateException("Cannot create stream with content-length of 0!");
        if (out != null)
            throw new IllegalStateException("This StreamResponse was already started!");

        out = getClient().getSocket().getOutputStream();

        if (!hasHeader("Content-Type"))
            addHeader("Content-Type", "text/html; charset=UTF-8");


        addHeader("Content-Length", "" + contentLength);

        String raw = "HTTP/1.1 " + getStatusCode().getCode() + " " + getStatusCode().getName() + "\n";
        for (String property : getHeaders().keySet()) {
            raw += property + ": " + getHeaders().get(property) + "\n";
        }
        raw += "\n";

        byte[] rawHeaderData = raw.getBytes(Charset.forName("ASCII"));

        out.write(rawHeaderData);

        return out;
    }

    /**
     * Get the {@link java.io.OutputStream} of this response. If {@link StreamResponse#startStream()} was not invoked, then
     * null will be returned
     * @return The {@link java.io.OutputStream} of this response or null if no stream was started.
     */
    public OutputStream getOutputStream() {
        return out;
    }

    @Override
    public StreamResponse createStreamResponse() {
        throw new UnsupportedOperationException("Cannot create a StreamResponse from a StreamResponse object!");
    }

    @Override
    public StreamResponse createStreamResponse(long l) {
        throw new UnsupportedOperationException("Cannot create a StreamResponse from a StreamResponse object!");
    }
}