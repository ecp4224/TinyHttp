# TinyHttp

TinyHttp is a small, lightweight http server API for Java. It allows you to dynamiclly respond to requests in java code.

## Features

* Respond dynamiclly to responses using Java code
* Stream large data using StreamResponse API
* Quickly launch a http server to simply serve files
* Handle webhooks from services such as [IFTTT](https://ifttt.com/discover)

## How to respond dynamiclly

TinyHttp allows you to dynamiclly respond to requests made by a client using java code. This can be acomplished by creating
a `TinyListener` class.

An example listener class looks something like this:

```java
public class DefaultTinyListener implements TinyListener {

    @GetHandler(request = "/wat.*")
    public void defaultGet(Request request, Response response) {
        response.echo("hi");
    }

    @GetHandler(request = "/index.html|/")
    public void index(Request request, Response response) {
        response.echo(
                "<html>\n" +
                "    <body>\n" +
                "        <h1>Hello " + info.getClient().getSocket().getInetAddress().toString().substring(1) + "!</h1>\n" +
                "    </body>\n" +
                "</html>"
        );
    }

    @GetHandler(request = "/api/[a-z]+")
    @PostHandler(request = "/api/[a-z]+")
    public void api(Request request, Response response) {
        response.setStatusCode(StatusCode.Found);
        response.echo("You requested " + request.getFileRequest());
    }
}
```

Each method with a `GetHandler` or `PostHandler` annotation is invoked when a client connects with a request matching one of the methods annotations.
The annotations can speicfy a static string or regex, so you can tie multiple kinds of requests to a single method.

Data about the request is provided through the `Request` object in the methods, and you respond using functions provided by the `Response` object.
