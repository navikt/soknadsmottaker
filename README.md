Soknadsmottaker
================
Applikasjonen tilbyr en REST-tjeneste for å motta metadata om innsendte søknader/dokumentinnsendinger.
Mottatte data mappes til en Avro-melding og legges på en egen Kafkastrøm.

This application provides a REST endpoint to which data can be sent. The data will be converted, serialized as an Avro message and put on a Kafka topic.
For a description of the whole archiving system, see [the documentation](https://github.com/navikt/archiving-infrastructure/wiki).

# Dependencies
This component requires the following to work:
* [soknadarkiv-schema](https://github.com/navikt/soknadarkiv-schema) (Avro schema definitions)
* Kafka broker (for providing a Kafka topic to send to)
* Shared secret for logging on to the application in order to send application data to the REST endpoint. The application reads the shared secret from Vault, restPassword and restUser.


## Inquiries
Questions regarding the code or the project can be asked to [team-soknad@nav.no](mailto:team-soknad@nav.no)

### For NAV employees
NAV employees can reach the team by Slack in the channel #teamsoknad
