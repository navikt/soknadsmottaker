FROM ghcr.io/navikt/baseimages/temurin:17

ENV SPRING_PROFILES_ACTIVE="docker"
ENV KAFKA_BROKERS=kafka-broker:29092
ENV KAFKA_SCHEMA_REGISTRY=http://kafka-schema-registry:8081

COPY mottaker/target/*.jar app.jar
