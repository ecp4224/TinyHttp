package me.eddiep.tinyhttp.net;

import me.eddiep.tinyhttp.TinyHttpServer;
import me.eddiep.tinyhttp.net.http.HttpMethod;
import me.eddiep.tinyhttp.net.http.MimeTypes;
import me.eddiep.tinyhttp.net.http.StatusCode;

import java.io.*;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;

public class Request {
    private Client client;
    private String requestPath;
    private HttpMethod method;
    private String httpVersion;
    private TinyHttpServer server;
    private HashMap<String, String> headers = new HashMap<String, String>();
    private BufferedReader contentStream;

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

    void setRawContentStream(BufferedReader stream) {
        this.contentStream = stream;
    }

    /**
     * Get the {@link java.io.InputStream} used for reading the enclosed content of this request.
     * If no content is enclosed in this request, then this method will return null
     * @return The {@link java.io.InputStream} used for reading the enclosed content or null if no content exists
     */
    public BufferedReader getContentStream() {
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
                char[] data = new char[server.getBufferDataLength()];
                StringBuilder builder = new StringBuilder();
                while (contentStream.read(data) != -1) {
                    builder.append(new String(data, 0, data.length));
                }
                return builder.toString();
            } else {
                byte[] data = new byte[(int)length];
                for (int i = 0; i < length; i++) {
                    int read = contentStream.read();
                    if (read == -1)
                        break;
                    data[i] = (byte)read;
                }
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

    public Response serveFile(String filePath, Response respond) {
        if (filePath == null || filePath.isEmpty())
            filePath = "index.html";

        String path = server.getRootDirectoryAsString() + filePath;

        File file = new File(path);
        return serveFile(file, respond);
    }

    public Response serveFile(File file, Response respond) {
        if (file.exists()) {
            try {
                String mime = MimeTypes.getMimeTypeFor(file);
                if (mime == null)
                    mime = "application/octet-stream";

                respond.setStatusCode(StatusCode.OK);
                respond.setContentType(mime);
                readIntoResponse(file, respond);
            } catch (AccessDeniedException e) {
                respond.setStatusCode(StatusCode.Forbidden);
                System.err.println("Error serving request for " + getClient().getSocket().getInetAddress() + " requesting " + getRequestPath());
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("Error serving request for " + getClient().getSocket().getInetAddress() + " requesting " + getRequestPath());
                respond.setStatusCode(StatusCode.InternalServerError);
                e.printStackTrace();
            }

            return respond;
        }

        respond.setStatusCode(StatusCode.NotFound);
        return respond;
    }

    protected void readIntoResponse(File file, Response respond) throws IOException {
        if (file.length() >= Integer.MAX_VALUE) {
            StreamResponse streamResponse = respond.createStreamResponse(file.length());

            byte[] buffer = new byte[server.getBufferDataLength()];
            InputStream ios = new FileInputStream(file);
            OutputStream out = streamResponse.startStream();
            int read;
            while ((read = ios.read(buffer)) != -1)
                out.write(buffer, 0, read);
            ios.close();
        } else {
            byte[] buffer = new byte[(int) file.length()];
            InputStream ios = new FileInputStream(file);
            if (ios.read(buffer) == -1)
                throw new IOException("EOF reached while trying to read whole file!");
            ios.close();

            respond.setRawContent(buffer);
        }
    }
}
