FROM  azul/zulu-openjdk-alpine:17-latest

MAINTAINER royse

LABEL authors="royse"

RUN mkdir -p /app/bootcamp-framework-microservice

WORKDIR /app/bootcamp-framework-microservice

COPY ./target/bootcamp-framework-microservice-0.0.1.jar /app/bootcamp-framework-microservice/bootcamp-framework-microservice-0.0.1.jar

EXPOSE 8080

ENTRYPOINT java -Xms256m -Xmx256m -jar bootcamp-framework-microservice-0.0.1.jar

