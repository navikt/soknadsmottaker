FROM navikt/java:11
#FROM azul/zulu-openjdk-alpine:12

COPY ./service/target/service-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8090
