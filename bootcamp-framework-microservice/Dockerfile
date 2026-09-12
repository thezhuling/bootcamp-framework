# The project compiles with maven.compiler.release=25, so the runtime must be
# Java 25 — the previous 17 base image could not load these class files at all.
# JRE rather than JDK: nothing is compiled inside the container.
FROM azul/zulu-openjdk-alpine:25-jre

LABEL authors="royse"

WORKDIR /app/bootcamp-framework-microservice

# Requires the repackaged (executable) jar produced by spring-boot-maven-plugin's
# repackage goal — see the plugin's executions block in the root pom.
COPY ./target/bootcamp-framework-microservice-0.0.1.jar app.jar

EXPOSE 8080

# Exec form so the JVM is PID 1 and receives SIGTERM directly; with the previous
# shell form the signal went to /bin/sh and shutdown was never graceful.
ENTRYPOINT ["java", "-Xms256m", "-Xmx256m", "-jar", "app.jar"]
