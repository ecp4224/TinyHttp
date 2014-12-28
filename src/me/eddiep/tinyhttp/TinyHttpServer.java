package me.eddiep.tinyhttp;

import me.eddiep.tinyhttp.annotations.DeleteHandler;
import me.eddiep.tinyhttp.annotations.GetHandler;
import me.eddiep.tinyhttp.annotations.PostHandler;
import me.eddiep.tinyhttp.annotations.PutHandler;
import me.eddiep.tinyhttp.test.impl.DefaultTinyListener;
import me.eddiep.tinyhttp.system.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TinyHttpServer {
    private ArrayList<Client> connectedClients = new ArrayList<Client>();
    private int port;
    private int timeout = 300000;
    private boolean running;
    private ServerSocket server;
    private TinyListener listener;
    private ArrayList<Request> toInvoke;

    public TinyHttpServer(TinyListener listener) {
        this(80, listener);
    }

    public TinyHttpServer(int port, TinyListener listener) {
        this.port = port;
        this.listener = listener;
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

    public final int getTimeout() {
        return timeout;
    }

    public final void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public final void start() throws IOException {
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

    public final void stop() throws IOException {
        onStop();
        if (running) {
            throw new IllegalStateException("super.onStop was not invoked!");
        }
    }

    protected void onStop() {
        running = false;
    }

    protected void onStart() {
        toInvoke = new ArrayList<Request>();

        Method[] methods = listener.getClass().getDeclaredMethods();
        for (Method m : methods) {
            if (m.getParameterTypes().length != 2 || m.getParameterTypes()[0] != RequestInfo.class || m.getParameterTypes()[1] != Respond.class) continue;

            GetHandler get = m.getAnnotation(GetHandler.class);
            DeleteHandler delete = m.getAnnotation(DeleteHandler.class);
            PostHandler post = m.getAnnotation(PostHandler.class);
            PutHandler put = m.getAnnotation(PutHandler.class);

            if (get != null) {
                Request request = new Request();
                m.setAccessible(true);
                request.invoke = m;
                request.requestPath = get.request();
                request.method = HttpMethod.GET;
                toInvoke.add(request);
            }
            if (delete != null) {
                Request request = new Request();
                m.setAccessible(true);
                request.invoke = m;
                request.requestPath = delete.request();
                request.method = HttpMethod.DELETE;
                toInvoke.add(request);
            }
            if (post != null) {
                Request request = new Request();
                m.setAccessible(true);
                request.invoke = m;
                request.requestPath = post.request();
                request.method = HttpMethod.POST;
                toInvoke.add(request);
            }
            if (put != null) {
                Request request = new Request();
                m.setAccessible(true);
                request.invoke = m;
                request.requestPath = put.request();
                request.method = HttpMethod.PUT;
                toInvoke.add(request);
            }
        }
    }

    public String currentDate() {
        DateFormat df = new SimpleDateFormat("EEE, WW MMMM yyyy HH:mm:ss zzz");
        Date today = Calendar.getInstance().getTime();
        return df.format(today);
    }

    public Respond invokeForRequest(RequestInfo request) {
        Respond respond = new Respond(request.getClient());
        respond.addHeader("Date", currentDate());
        respond.addHeader("Server", "TinyHttpServer/1.0 (" + System.getProperty("os.name") + ")");

        for (Request temp : toInvoke) {
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

        respond.setStatusCode(StatusCode.NotFound);
        return respond;
    }

    public void closeClient(Client client) {
        client.close();
        connectedClients.remove(client);
    }

    private class Request {
        public String requestPath;
        public HttpMethod method;
        public Method invoke;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Request request = (Request) o;

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
