FROM navikt/java:8
LABEL maintainer="Team Søknad"

COPY target/*SNAPSHOT.jar /app.jar

CMD java -jar /app.jar
