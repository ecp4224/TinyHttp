package me.eddiep.tinyhttp.system;

import me.eddiep.tinyhttp.TinyHttpServer;

import java.io.*;
import java.net.Socket;

public class Client extends Thread {
    private Socket client;
    private TinyHttpServer server;
    private BufferedWriter writer;
    private BufferedReader reader;

    public Client(Socket client, TinyHttpServer server) {
        this.client = client;
        this.server = server;
    }

    public Socket getSocket() {
        return client;
    }

    public TinyHttpServer getServer() {
        return server;
    }

    @Override
    public void run() {
        try {
            writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

            String request = reader.readLine();
            if (request == null) {
                server.closeClient(this);
                return;
            }
            String[] info = request.split(" ");
            if (info.length != 3) {
                server.closeClient(this);
                return;
            }

            RequestInfo requestInfo = new RequestInfo(info[1], HttpMethod.toHttpMethod(info[0]), info[2], this, server);

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

            Respond respond = server.invokeForRequest(requestInfo);

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
            server.closeClient(this);
        }
    }

    public void close() {
        try {
            writer.close();
            reader.close();

            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
