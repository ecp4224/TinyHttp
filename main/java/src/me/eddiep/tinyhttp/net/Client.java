package me.eddiep.tinyhttp.net;

import me.eddiep.tinyhttp.TinyHttpServer;
import me.eddiep.tinyhttp.net.http.HttpMethod;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class Client {
    private boolean started;
    private Thread thread;
    private Socket client;
    private TinyHttpServer server;
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
                /*byte[] test = new byte[65536];
                int i = client.getInputStream().read(test);
                for (int z = 0; z < i; z++) {
                    System.out.print((char)test[z]);
                }*/

                reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

                int readCount = 0;
                String request = reader.readLine();
                if (request == null) {
                    server.closeClient(Client.this);
                    return;
                }
                readCount += request.length();
                String[] info = request.split(" ");
                if (info.length != 3) {
                    server.closeClient(Client.this);
                    return;
                }

                Request requestInfo = new Request(info[1], HttpMethod.toHttpMethod(info[0]), info[2], Client.this, server);

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().equals(""))
                        break;
                    if (line.split(":").length > 1) {
                        String property = line.split(":")[0].trim();
                        String value = line.split(":")[1].trim();

                        requestInfo.addHeader(property, value);
                    }
                    readCount += line.length();
                }

                if ((requestInfo.getRequestMethod() == HttpMethod.POST || requestInfo.getRequestMethod() == HttpMethod.PUT) && requestInfo.hasHeader("Content-Length")) {
                    requestInfo.setRawContentStream(reader);
                }

                String encoding = "utf-8";
                if (requestInfo.hasHeader("Accept-Charset"))
                    encoding = requestInfo.getHeaderValue("Accept-Charset");

                Response respond = new Response(Client.this);

                respond = server.invokeForRequest(requestInfo, respond);

                if (respond instanceof StreamResponse && ((StreamResponse)respond).getOutputStream() != null) {
                    ((StreamResponse)respond).getOutputStream().flush();
                } else if (respond.streamResponse != null && respond.streamResponse.getOutputStream() != null) {
                    respond.streamResponse.getOutputStream().flush();
                } else {
                    if (!respond.hasHeader("Content-Type"))
                        respond.addHeader("Content-Type", "text/html; charset=UTF-8");

                    if (respond.rawContents != null)
                        respond.addHeader("Content-Length", "" + respond.rawContents.length);
                    else
                        respond.addHeader("Content-Length", "" + respond.getContent().length());

                    String raw = "HTTP/1.1 " + respond.getStatusCode().getCode() + " " + respond.getStatusCode().getName() + "\r\n";
                    for (String property : respond.getHeaders().keySet()) {
                        raw += property + ": " + respond.getHeaders().get(property) + "\r\n";
                    }
                    raw += "\r\n";

                    if (respond.rawContents != null) {
                        byte[] rawHeaderData = raw.getBytes(Charset.forName("ASCII"));

                        client.getOutputStream().write(rawHeaderData);
                        client.getOutputStream().write(respond.rawContents);
                    } else {
                        byte[] rawHeaderData = raw.getBytes(Charset.forName("ASCII"));
                        byte[] rawContent = respond.getContent().getBytes(Charset.forName(encoding));

                        client.getOutputStream().write(rawHeaderData);
                        client.getOutputStream().write(rawContent);
                    }

                    client.getOutputStream().flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                server.closeClient(Client.this);
            }
        }
    };
}
