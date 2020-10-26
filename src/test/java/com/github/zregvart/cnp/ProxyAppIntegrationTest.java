/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.zregvart.cnp;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;

import org.apache.camel.main.BaseMainSupport;
import org.apache.camel.main.Main;
import org.apache.camel.main.MainListenerSupport;
import org.apache.camel.main.SimpleMainShutdownStrategy;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.tomakehurst.wiremock.WireMockServer;

import static org.assertj.core.api.Assertions.assertThat;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;

import ru.lanwen.wiremock.ext.WiremockResolver;
import ru.lanwen.wiremock.ext.WiremockResolver.Wiremock;
import ru.lanwen.wiremock.ext.WiremockUriResolver;
import ru.lanwen.wiremock.ext.WiremockUriResolver.WiremockUri;

@ExtendWith({
    WiremockResolver.class,
    WiremockUriResolver.class
})
class ProxyAppIntegrationTest {

    final static SimpleMainShutdownStrategy SHUTDOWN = new SimpleMainShutdownStrategy();

    @Test
    public void shouldProxyPlaintextHTTP(@Wiremock final WireMockServer server, @WiremockUri final String backend) throws Exception {
        server.stubFor(post("/path?a=b")
            .withQueryParam("a", equalTo("b"))
            .withHost(equalTo("localhost"))
            .withHeader("x", equalTo("y"))
            .withRequestBody(equalTo("HI"))
            .willReturn(aResponse().withBody("hello")));

        final HttpClient client = HttpClient.newBuilder()
            .proxy(ProxySelector.of(new InetSocketAddress("localhost", 8080)))
            .build();

        final HttpResponse<String> response = client.send(
            HttpRequest.newBuilder(URI.create(backend).resolve("/path?a=b"))
                .POST(BodyPublishers.ofString("hi"))
                .header("x", "y")
                .build(),
            BodyHandlers.ofString());

        assertThat(response.body()).isEqualTo("HELLO");
    }

    @BeforeAll
    static void launchMain() throws InterruptedException {
        final Main main = new Main();
        final CountDownLatch started = new CountDownLatch(1);
        main.addMainListener(new MainListenerSupport() {
            @Override
            public void afterStart(final BaseMainSupport given) {
                started.countDown();
            }

            @Override
            public void afterStop(final BaseMainSupport main) {
                started.countDown();
            }
        });
        main.setShutdownStrategy(SHUTDOWN);
        ForkJoinPool.commonPool().submit(() -> ProxyApp.launch(main));
        started.await();
    }

    @AfterAll
    static void shutdownMain() {
        SHUTDOWN.shutdown();
    }
}
