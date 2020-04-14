[![Docker Repository on Quay](https://quay.io/repository/zregvart/camel-netty-proxy/status "Docker Repository on Quay")](https://quay.io/repository/zregvart/camel-netty-proxy) [![Docker Repository on Docker Hub](https://img.shields.io/docker/automated/zregvart/camel-netty-proxy.svg "Docker Repository on Docker Hub")](https://hub.docker.com/r/zregvart/camel-netty-proxy) [![CircleCI](https://circleci.com/gh/zregvart/camel-netty-proxy.svg?style=svg)](https://circleci.com/gh/zregvart/camel-netty-proxy)

# Camel Netty Proxy example

This is an example of using the [Apache Camel](https://camel.apache.org/staging/)
integration framework to create a HTTP proxy using the [Camel Netty](https://camel.apache.org/staging/components/latest/netty4-http-component.html)
component.

Any request that is received using the HTTP PROXY protocol, i.e specifying
the absolute form for the [request target](https://tools.ietf.org/html/rfc7230#section-5.3.2)
will be forwarded to the target service with the HTTP body converted to
uppercase. The response from the target service will be processed by converting
it to uppercase and returned to the client.

The support for HTTP over TLS (`https`) protocol is available if Java Keystore
file is mounted at `/tls/keystore.jks` (with password `changeit`). The
implementation doesn't support HTTPS proxy tunneling via `CONNECT`, the
request needs to be issued same as it is issued for the HTTP PROXY, the only
added benefit is that the request can be made over TLS.

If `/tls/keystore.jks` exists then the listening port changes to `8443`.

## Building and running

To build the Docker image execute:

    $ docker build -t camel-netty-proxy .

To run the Docker image execute:

    $ docker run --rm -p 8080:8080 camel-netty-proxy

To test using `curl` set the `http_proxy` environment variable, for example:

    $ http_proxy=http://localhost:8080 curl -v http://neverssl.com

## Running on OpenShift

To run on openshift, log in via the `oc login` command to the OpenShift cluster
and position yourself in the project of choice using `oc project <project>` and
run:

    $ ./mvnw -Popenshift package

When running on OpenShift the service listens on port 8443 with TLS enabled.
The certificate is issued by the OpenShift CA.
