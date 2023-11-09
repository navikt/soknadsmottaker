FROM gcr.io/distroless/java21-debian12:nonroot

ENV SPRING_PROFILES_ACTIVE="docker"
ENV KAFKA_BROKERS=kafka-broker:29092
ENV KAFKA_SCHEMA_REGISTRY=http://kafka-schema-registry:8081

COPY mottaker/target/*.jar /app/app.jar

WORKDIR /app

CMD ["app.jar"]
