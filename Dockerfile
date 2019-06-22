FROM maven:3-jdk-8 AS build
WORKDIR /build
COPY pom.xml settings.xml ./
RUN mvn -B -q -s settings.xml dependency:go-offline
COPY src src
RUN mvn -B -q -s settings.xml package -Dtarget=camel-netty-proxy
RUN mvn -B -q -s settings.xml package dependency:copy-dependencies

FROM openjdk:8-jre
WORKDIR /app
VOLUME /tmp
EXPOSE 8080
ARG TARGET=/build/target
COPY --from=build ${TARGET}/dependency lib
COPY --from=build ${TARGET}/camel-netty-proxy.jar .
ENTRYPOINT ["java", "-cp", "camel-netty-proxy.jar:lib/*", "com.github.zregvart.cnp.ProxyApp"]

