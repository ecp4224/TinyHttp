#TinyHttp
TinyHttp is a small, lightweight http server API for Java. It allows you to dynamiclly respond to requests in java code.

##Features

* Respond dynamiclly to responses using java code
* Serve files from the file system
* Stream large data using StreamResponse API
* Easy to use API

##How to respond dynamiclly

TinyHttp allows you to dynamiclly respond to requests made by a client using java code. This can be acomplished by creating
a TinyListener class.

An example listener class looks something like this:

```java
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
```

Each method with a handler annotation is invoked when a client connects with a request matching one of the methods annotations.
The annotations support regex, so you can tie multiple kinds of requests to a single method.
