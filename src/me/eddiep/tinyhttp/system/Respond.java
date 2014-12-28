package me.eddiep.tinyhttp.system;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Respond {
    private String content = "";
    private Client client;
    private StatusCode statusCode = StatusCode.OK;
    private HashMap<String, String> headers = new HashMap<String, String>();

    public Respond(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

    public void setStatusCode(StatusCode code) {
        this.statusCode = code;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public void addHeader(String property, String value) {
        this.headers.put(property, value);
    }

    public void removeHeader(String property) {
        this.headers.remove(property);
    }

    public boolean hasHeader(String property) {
        return headers.containsKey(property);
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void echo(String string) {
        content += string;
    }

    public void echo(int i)  {
        echo("" + i);
    }

    public void echo(float f) {
        echo("" + f);
    }

    public void echo(long l) {
        echo("" + l);
    }

    public void echo(boolean bool) {
        echo("" + bool);
    }

    public void echo(Object obj) {
        echo(obj.toString());
    }
}
