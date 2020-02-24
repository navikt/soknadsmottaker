FROM navikt/java:11
ENV APPLICATION_PROFILE="remote"

COPY target/*.jar app.jar
COPY .initscript /init-scripts
EXPOSE 8080
