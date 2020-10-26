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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.camel.model.RouteDefinition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class ProxyRouteTest {

    @TempDir
    Path keystoreDir;

    @FunctionalInterface
    interface IOSupplier<T> {
        T get() throws IOException;
    }

    @Test
    void shouldCreateStartEndpointWithoutTLSIfKeystoreDoesntExists() {
        final RouteDefinition definition = configureWithPath(() -> keystoreDir.resolve("nonexistant.p12"));

        assertThat(definition.getEndpointUrl()).doesNotContain("ssl=true");
    }

    @Test
    void shouldCreateStartEndpointWithTLSIfKeystoreExists() {
        final RouteDefinition definition = configureWithPath(() -> Files.createTempFile(keystoreDir, "keystore", ".p12"));

        assertThat(definition.getEndpointUrl()).contains("ssl=true");
    }

    private static RouteDefinition configureWithPath(final IOSupplier<Path> keystorePath) {
        final ProxyRoute route = new ProxyRoute() {
            @Override
            Path keystorePath() {
                try {
                    return keystorePath.get();
                } catch (final IOException e) {
                    throw new AssertionError(e);
                }
            }
        };

        try {
            route.configure();
        } catch (final Exception e) {
            throw new AssertionError(e);
        }

        final List<RouteDefinition> routes = route.getRouteCollection().getRoutes();

        assertThat(routes).hasSize(1);

        final RouteDefinition definition = routes.get(0);
        return definition;
    }
}
