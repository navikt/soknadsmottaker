FROM navikt/java:11
ENV APPLICATION_PROFILE="remote"

COPY target/*.jar app.jar

EXPOSE 8080
