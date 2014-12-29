package me.eddiep.tinyhttp.system;

import me.eddiep.tinyhttp.TinyHttpServer;

import java.io.*;
import java.net.Socket;

public class Client {
    private boolean started;
    private Thread thread;
    private Socket client;
    private TinyHttpServer server;
    private BufferedWriter writer;
    private BufferedReader reader;

    public Client(Socket client, TinyHttpServer server) {
        this.client = client;
        this.server = server;
    }

    /**
     * Get the socket for this client
     * @return The socket representing this client
     */
    public Socket getSocket() {
        return client;
    }

    /**
     * Get the {@link me.eddiep.tinyhttp.TinyHttpServer} this client came from
     * @return The {@link me.eddiep.tinyhttp.TinyHttpServer} this client came from
     */
    public TinyHttpServer getServer() {
        return server;
    }

    /**
     * Get the {@link java.lang.Thread} handling this client's requestPath
     * @return The {@link java.lang.Thread} object handling this client's requestPath
     */
    public Thread getHandlerThread() {
        return thread;
    }

    /**
     * Start handling this clients' requestPath.
     */
    public final void start() {
        if (started)
            throw new IllegalStateException("This clients' requestPath is either already being handled or has already been handled.");

        onStart();
        if (thread == null)
            throw new IllegalStateException("super.onStart() was not invoked!");

        if (server.isThreaded())
            thread.start();
        else
            HANDLE_RUNNABLE.run();
    }

    protected void onStart() {
        started = true;

        if (server.isThreaded())
            thread = new Thread(HANDLE_RUNNABLE);
        else
            thread = Thread.currentThread();
    }

    /**
     * Disconnect and dispose this client
     */
    public final void close() {
        if (!started)
            throw new IllegalStateException("This clients' requestPath has not been handled yet!");

        onClose();
        if (!client.isClosed())
            throw new IllegalStateException("This client was not closed properly! (Did you invoke super.onClose?)");
    }

    protected void onClose() {
        try {
            writer.close();
            reader.close();

            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final Runnable HANDLE_RUNNABLE = new Runnable() {
        @Override
        public void run() {
            try {
                writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

                String request = reader.readLine();
                if (request == null) {
                    server.closeClient(Client.this);
                    return;
                }
                String[] info = request.split(" ");
                if (info.length != 3) {
                    server.closeClient(Client.this);
                    return;
                }

                Request requestInfo = new Request(info[1], HttpMethod.toHttpMethod(info[0]), info[2], Client.this, server);

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.equals(""))
                        break;
                    if (line.split(":").length > 1) {
                        String property = line.split(":")[0].trim();
                        String value = line.split(":")[1].trim();

                        requestInfo.addHeader(property, value);
                    }
                }

                Response respond = new Response(Client.this);
                respond = server.invokeForRequest(requestInfo, respond);

                if (!respond.hasHeader("Content-Type"))
                    respond.addHeader("Content-Type", "text/html; charset=UTF-8");

                respond.addHeader("Content-Length", "" + respond.getContent().length());

                String raw = "HTTP/1.1 " + respond.getStatusCode().getCode() + " " + respond.getStatusCode().getName() + "\n";
                for (String property : respond.getHeaders().keySet()) {
                    raw += property + ": " + respond.getHeaders().get(property) + "\n";
                }
                raw += "\n" + respond.getContent();

                writer.write(raw);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                server.closeClient(Client.this);
            }
        }
    };
}
