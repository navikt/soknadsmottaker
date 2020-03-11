FROM navikt/java:11
ENV APPLICATION_PROFILE="docker"
ENV KAFKA_BOOTSTRAP_SERVERS=kafka-broker:29092

COPY target/*.jar app.jar
