package me.eddiep.tinyhttp.test.impl;

import me.eddiep.tinyhttp.TinyListener;
import me.eddiep.tinyhttp.annotations.GetHandler;
import me.eddiep.tinyhttp.annotations.PostHandler;
import me.eddiep.tinyhttp.system.RequestInfo;
import me.eddiep.tinyhttp.system.Respond;
import me.eddiep.tinyhttp.system.StatusCode;

public class DefaultTinyListener implements TinyListener {

    @GetHandler(request = "/wat.*")
    public void defaultGet(RequestInfo info, Respond respond) {
        respond.echo("hi");
    }

    @GetHandler(request = "/index.html|/")
    public void index(RequestInfo info, Respond respond) {
        respond.echo(
                "<html>\n" +
                "    <body>\n" +
                "        <h1>Hello " + info.getClient().getSocket().getInetAddress().toString().substring(1) + "!</h1>\n" +
                "    </body>\n" +
                "</html>"
        );
    }

    @GetHandler(request = "/api/[a-z]+")
    @PostHandler(request = "/api/[a-z]+")
    public void api(RequestInfo info, Respond respond) {
        respond.setStatusCode(StatusCode.Found);
        respond.echo("You requested " + info.getFileRequest());
    }
}
