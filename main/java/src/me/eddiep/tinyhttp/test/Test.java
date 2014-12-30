package me.eddiep.tinyhttp.test;

import me.eddiep.tinyhttp.TinyHttpServer;
import me.eddiep.tinyhttp.test.impl.DefaultTinyListener;

import java.io.IOException;

public class Test {
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
