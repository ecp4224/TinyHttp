package me.eddiep.tinyhttp.test.impl;

import me.eddiep.tinyhttp.TinyListener;
import me.eddiep.tinyhttp.annotations.GetHandler;
import me.eddiep.tinyhttp.annotations.PostHandler;
import me.eddiep.tinyhttp.system.Request;
import me.eddiep.tinyhttp.system.Response;
import me.eddiep.tinyhttp.system.StatusCode;

public class DefaultTinyListener implements TinyListener {

    @GetHandler(requestPath = "/wat.*")
    public void defaultGet(Request info, Response respond) {
        respond.echo("hi");
    }

    @GetHandler(requestPath = "/api/[a-z]+")
    @PostHandler(requestPath = "/api/[a-z]+")
    public void api(Request info, Response respond) {
        respond.setStatusCode(StatusCode.Found);
        respond.echo("You requested " + info.getFileRequest());
    }
}
