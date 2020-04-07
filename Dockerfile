FROM navikt/java:11
ENV APPLICATION_PROFILE="docker"
ENV KAFKA_BOOTSTRAP_SERVERS=kafka-broker:29092
ENV SCHEMA_REGISTRY_URL=http://kafka-schema-registry:8081

COPY target/*.jar app.jar
