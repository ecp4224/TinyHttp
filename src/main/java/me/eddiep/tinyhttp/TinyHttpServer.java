package me.eddiep.tinyhttp;

import me.eddiep.tinyhttp.annotations.DeleteHandler;
import me.eddiep.tinyhttp.annotations.GetHandler;
import me.eddiep.tinyhttp.annotations.PostHandler;
import me.eddiep.tinyhttp.annotations.PutHandler;
import me.eddiep.tinyhttp.net.*;
import me.eddiep.tinyhttp.net.http.HttpMethod;
import me.eddiep.tinyhttp.net.http.MimeTypes;
import me.eddiep.tinyhttp.net.http.StatusCode;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.AccessDeniedException;
import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A tinyhttp server. This class can be extended and the start and stop behavior can be modified
 * by overriding the {@link TinyHttpServer#onStart()} method and the {@link me.eddiep.tinyhttp.TinyHttpServer#onStop()} method
 */
public class TinyHttpServer {
    private boolean serveFileSystem = true;
    private int bufferDataLength = 1024;
    private String root = "";
    private ArrayList<Client> connectedClients = new ArrayList<Client>();
    private int port;
    private int timeout = 5000;
    private boolean running;
    private ServerSocket server;
    private boolean threaded = true;
    private TinyListener listener;
    private ArrayList<RequestHolder> toInvoke;

    /**
     * Create a new instance of a TinyHttpServer with the default http port
     * @param listener The listener class that will be handling requests
     */
    public TinyHttpServer(TinyListener listener) {
        this(80, listener);
    }

    /**
     * Create a new instance of a TinyHttpServer with a custom http port to listen to
     * @param port The port to listen to
     * @param listener The listener class that will be handling requests
     */
    public TinyHttpServer(int port, TinyListener listener) {
        this(port, listener, true);
    }

    /**
     * Create a new instance of a TinyHttpServer with a custom http port to listen to
     *
     * @see me.eddiep.tinyhttp.TinyHttpServer#serveFileSystem(boolean)
     * @param port The port to listen to
     * @param listener The listener class that will be handling requests
     * @param serveFileSystem Whether this tinyhttp server will serve the filesystem. See {@link me.eddiep.tinyhttp.TinyHttpServer#serveFileSystem(boolean)}
     */
    public TinyHttpServer(int port, TinyListener listener, boolean serveFileSystem) {
        this.port = port;
        this.listener = listener;
        this.serveFileSystem = serveFileSystem;

        if (serveFileSystem)
            MimeTypes.loadMimeTypes();
    }

    /**
     * Get the root path for this tinyhttp server. The root path is used to find static files if
     * no method in the {@link me.eddiep.tinyhttp.TinyListener} class handled the client's request.
     * @return The root directory as a {@link java.lang.String}
     */
    public final String getRootDirectoryAsString() {
        return root;
    }

    /**
     * Get the root path for this tinyhttp server. The root path is used to find static files if
     * no method in the {@link me.eddiep.tinyhttp.TinyListener} class handled the client's request.
     * @return The root directory as a {@link java.io.File}
     */
    public final File getRootDirectoryAsFile() {
        return new File(root);
    }


    /**
     * Set the root path for this tinyhttp server. The root path is used to find static files if
     * no method in the {@link me.eddiep.tinyhttp.TinyListener} class handled the client's request. <br></br>
     * If the root directory does not exist, then the directory and any necessary nonexistent parent directories will be made
     *
     * @see java.io.File#mkdirs()
     * @param dir The directory to use as the root directory, as a {@link java.io.File}
     */
    public final void setRootDirectory(File dir) {
        if (!dir.isDirectory())
            throw new IllegalArgumentException("The root provided is not a directory!");
        if (!dir.exists()) {
            if (!dir.mkdirs())
                throw new RuntimeException("Failed to create root directory!");
        }
        root = dir.getAbsolutePath();
    }

    /**
     * Set the root path for this tinyhttp server. The root path is used to find static files if
     * no method in the {@link me.eddiep.tinyhttp.TinyListener} class handled the client's request. <br></br>
     * This method simply creates a {@link java.io.File} object with the specified path and invokes {@link me.eddiep.tinyhttp.TinyHttpServer#setRootDirectory(java.io.File)}
     * @param path The path to use as the root directory, as a {@link java.lang.String}
     */
    public final void setRootDirectory(String path) {
        File dir = new File(path);
        setRootDirectory(dir);
    }

    /**
     * Indicates whether or not this tinyhttp server will process all requests ignored by the {@link me.eddiep.tinyhttp.TinyListener}
     * class by looking for a file in the root directory specified in {@link TinyHttpServer#getRootDirectoryAsFile()}
     * @return If true, then this tinyhttp server will serve the filesystem when the {@link me.eddiep.tinyhttp.TinyListener} class ignores the request
     */
    public final boolean isServingFileSystem() {
        return serveFileSystem;
    }

    /**
     * Set whether or not this tinyhttp server will process all requests ignored by the {@link me.eddiep.tinyhttp.TinyListener}
     * class by looking for a file in the root directory specified in {@link TinyHttpServer#getRootDirectoryAsFile()}
     * @param value  If true, then this tinyhttp server will serve the filesystem when the {@link me.eddiep.tinyhttp.TinyListener} class ignores the request
     */
    public final void serveFileSystem(boolean value) {
        this.serveFileSystem = value;

        if (serveFileSystem && !MimeTypes.isLoaded())
            MimeTypes.loadMimeTypes();
    }

    /**
     * Get the buffer length used when sending large files and reading large files. tinyhttp uses this when {@link TinyHttpServer#isServingFileSystem()} is true and
     * when sending large data, and when a large POST/PUT request is made.
     * @return The buffer data length used when sending large data
     */
    public final int getBufferDataLength() {
        return bufferDataLength;
    }

    /**
     * Set the buffer length used when sending large files and reading large files. tinyhttp uses this when {@link TinyHttpServer#isServingFileSystem()} is true and
     * when sending large data, and when a large POST/PUT request is made.
     * @param length The buffer data length to use
     */
    public final void setBufferDataLength(int length) {
        this.bufferDataLength = length;
    }

    /**
     * The port this http server is listening on
     * @return The port number
     */
    public final int getPort() {
        return port;
    }

    /**
     * Returns whether or not the server is running
     * @return Whether or not the server is running
     */
    public final boolean isRunning() {
        return running;
    }

    /**
     * Get the timeout length in milliseconds.
     *
     * @see java.net.Socket#getSoTimeout()
     * @return The timeout in milliseconds
     */
    public final int getTimeout() {
        return timeout;
    }

    /**
     * Set the timeout for all future clients. This will not affect any clients already
     * connected
     * @see Socket#setSoTimeout(int)
     * @param timeout The timeout to set future clients to
     */
    public final void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Whether or not this tinyhttp server is handling client requests in a different thread <br></br>
     * If the server is threaded, then multiple clients requests can be handled at once, if the server is not
     * threaded, then clients requests will be handled one at a time
     * @return Whether or not this tinyhttp server is threaded
     */
    public final boolean isThreaded() {
        return threaded;
    }

    /**
     * Set whether or not this tinyhttp server will handle clients requests in a different thread. <br></br>
     * If the server is threaded, then multiple clients requests can be handled at once, if the server is not
     * threaded, then clients requests will be handled one at a time
     * @param threaded Whether or not this tinyhttp server is threaded
     */
    public final void setIsThreaded(boolean threaded) {
        this.threaded = threaded;
    }

    /**
     * Get an unmodifiable list of currently connected clients.
     *
     * @see Collections#unmodifiableList(java.util.List)
     * @return An unmodifiable list of currently connected clients
     */
    public final List<Client> getConnectedClients() {
        return Collections.unmodifiableList(connectedClients);
    }

    /**
     * Start this tinyhttp server and listen for new clients. <br></br>
     * This will block the calling thead until the server is stopped with {@link TinyHttpServer#stop()}
     * @throws IOException If there was an error starting the server
     */
    public final void start() throws IOException {
        if (running)
            throw new IllegalStateException("This server is already running!");

        onStart();

        if (toInvoke == null) {
            throw new IllegalStateException("super.onStart() was not invoked!");
        }

        server = new ServerSocket(port);
        running = true;

        Socket connection;
        while (running) {
            if (server.isClosed())
                break;
            try {
                connection = server.accept();
                connection.setSoTimeout(timeout);
                Client client = new Client(connection, this);
                connectedClients.add(client);
                client.start();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /**
     * Stop this tinyhttp server.
     */
    public final void stop() throws IOException {
        if (!running)
            throw new IllegalStateException("This server is not running!");

        onStop();
        if (running) {
            throw new IllegalStateException("super.onStop was not invoked!");
        }
    }

    protected void onStop() throws IOException {
        server.close();
        running = false;
    }

    protected void onStart() {
        toInvoke = new ArrayList<RequestHolder>();

        if (listener == null)
            return;

        Method[] methods = listener.getClass().getDeclaredMethods();
        for (Method m : methods) {
            if (m.getParameterTypes().length != 2 || m.getParameterTypes()[0] != me.eddiep.tinyhttp.net.Request.class || m.getParameterTypes()[1] != Response.class) continue;

            GetHandler get = m.getAnnotation(GetHandler.class);
            DeleteHandler delete = m.getAnnotation(DeleteHandler.class);
            PostHandler post = m.getAnnotation(PostHandler.class);
            PutHandler put = m.getAnnotation(PutHandler.class);

            if (get != null) {
                RequestHolder request = new RequestHolder();
                m.setAccessible(true);
                request.invoke = m;
                request.requestPath = get.requestPath();
                request.method = HttpMethod.GET;
                toInvoke.add(request);
            }
            if (delete != null) {
                RequestHolder request = new RequestHolder();
                m.setAccessible(true);
                request.invoke = m;
                request.requestPath = delete.requestPath();
                request.method = HttpMethod.DELETE;
                toInvoke.add(request);
            }
            if (post != null) {
                RequestHolder request = new RequestHolder();
                m.setAccessible(true);
                request.invoke = m;
                request.requestPath = post.requestPath();
                request.method = HttpMethod.POST;
                toInvoke.add(request);
            }
            if (put != null) {
                RequestHolder request = new RequestHolder();
                m.setAccessible(true);
                request.invoke = m;
                request.requestPath = put.requestPath();
                request.method = HttpMethod.PUT;
                toInvoke.add(request);
            }
        }
    }

    private String currentDate() {
        DateFormat df = new SimpleDateFormat("EEE, WW MMMM yyyy HH:mm:ss zzz");
        Date today = Calendar.getInstance().getTime();
        return df.format(today);
    }

    /**
     * Handle a requestPath sent by a client
     * @param request The requestPath info sent by the client
     * @return The response created by either the {@link me.eddiep.tinyhttp.TinyListener} object or by this tinyhttp server
     */
    public Response invokeForRequest(Request request, Response respond) {
        if (request.getClient() == null)
            throw new InvalidParameterException("No client specified in the requestPath!");

        respond.addHeader("Date", currentDate());
        respond.addHeader("Server", "TinyHttpServer/1.0 (" + System.getProperty("os.name") + ")");

        for (RequestHolder temp : toInvoke) {
            if (temp.method == request.getRequestMethod()) {
                if (request.getRequestPath().matches(temp.requestPath)) {
                    try {
                        temp.invoke.invoke(listener, request, respond);
                    } catch (IllegalAccessException e) {
                        respond.setStatusCode(StatusCode.InternalServerError);
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        respond.setStatusCode(StatusCode.InternalServerError);
                        e.printStackTrace();
                    } catch (Throwable e) {
                        respond.setStatusCode(StatusCode.InternalServerError);
                        e.printStackTrace();
                    }
                    return respond;
                }
            }
        }

        if (serveFileSystem) {
            String path = request.getRequestPath().substring(1);
            return request.serveFile(path, respond);
        }

        respond.setStatusCode(StatusCode.NotFound);
        return respond;
    }

    /**
     * Disconnect and dispose the client
     * @param client The client to dispose and disconnect
     */
    public void closeClient(Client client) {
        client.close();
        connectedClients.remove(client);
    }

    private class RequestHolder {
        public String requestPath;
        public HttpMethod method;
        public Method invoke;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RequestHolder request = (RequestHolder) o;

            if (method != request.method) return false;
            if (requestPath != null ? !requestPath.equals(request.requestPath) : request.requestPath != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = requestPath != null ? requestPath.hashCode() : 0;
            result = 31 * result + method.hashCode();
            return result;
        }
    }
}
