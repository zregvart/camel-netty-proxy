# Camel Netty Proxy example

This is an example of using the [Apache Camel](https://camel.apache.org/staging/)
integration framework to create a HTTP proxy using the [Camel Netty](https://camel.apache.org/staging/components/latest/netty4-http-component.html)
component.

Any request that is received using the HTTP PROXY protocol, i.e specifying
the absolute form for the [request target](https://tools.ietf.org/html/rfc7230#section-5.3.2)
will be forwarded to the target service with the HTTP body converted to
uppercase. The response from the target service will be processed by converting
it to uppercase and returned to the client.

There is no support for HTTP over TLS (`https`) protocol in this example.

## Building and running

To build the Docker image execute:

    $ docker build -t camel-netty-proxy .

To run the Docker image execute:

    $ docker run --rm -p 8080:8080 camel-netty-proxy

To test using `curl` set the `http_proxy` environment variable, for example:

    $ http_proxy=http://localhost:8080 curl -v http://neverssl.com


