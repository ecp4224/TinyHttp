package me.eddiep.tinyhttptest;

import me.eddiep.tinyhttp.TinyHttpServer;
import me.eddiep.tinyhttptest.impl.DefaultTinyListener;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        TinyHttpServer server = new TinyHttpServer(1234, new DefaultTinyListener());
        try {
            server.setBufferDataLength(268435456);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
