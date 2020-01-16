FROM navikt/java:11
#FROM azul/zulu-openjdk-alpine:12

COPY target/soknadsmottaker.jar /app/app.jar
EXPOSE 8090
