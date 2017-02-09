package me.eddiep.tinyhttp;

import java.io.IOException;

public class DefaultServer {

    public static void main(String[] args) throws IOException {
        TinyHttpServer server = new TinyHttpServer(8080, null, true);
        server.setBufferDataLength(268435456);
        server.start();
    }
}
