FROM navikt/java:17
ENV SPRING_PROFILES_ACTIVE="docker"
ENV KAFKA_BOOTSTRAP_SERVERS=kafka-broker:29092
ENV SCHEMA_REGISTRY_URL=http://kafka-schema-registry:8081

COPY mottaker/target/*.jar app.jar
