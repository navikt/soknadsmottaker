FROM gcr.io/distroless/java21-debian12:nonroot

ENV SPRING_PROFILES_ACTIVE="docker"
ENV KAFKA_BROKERS=kafka-broker:29092
ENV KAFKA_SCHEMA_REGISTRY=http://kafka-schema-registry:8081

ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb:NO.UTF-8' TZ="Europe/Oslo"

COPY mottaker/target/*.jar /app/app.jar

WORKDIR /app

CMD ["app.jar"]
