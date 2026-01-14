# Soknadsmottaker

When a user applies for a benefit (_sender inn en søknad_), one or more documents are sent in to NAV. This component
acts as a REST-endpoint to which the systems that the user uses can send metadata about the benefit. The documents
themselves are sent to a different system, [Soknadsfillager](https://www.github.com/navikt/soknadsfillager).

When Soknadsmottaker receives data, it will be converted, serialized as an Avro message and put on a Kafka topic.

Soknadsmottaker also has REST-interfaces for publishing _brukernotifikasjoner_ to Kafka topics.

For a description of the whole archiving system,
see [the documentation](https://github.com/navikt/archiving-infrastructure/wiki).

# Dependencies

This component requires the following to work:

* [soknadarkiv-schema](https://github.com/navikt/soknadarkiv-schema) (Avro schema definitions)
* Kafka broker (for providing Kafka topics to send to)

## Secure logs

The application will log the requests it receives, but mask the personId / fødselsnummer / fnr. If for debugging and
resending purposes, the fnr is needed. It can be found in the team log for the application, accessible via
https://console.cloud.google.com/projectselector2/logs/query?supportedpurview=folder,organizationId,project&authuser=1.


## Inquiries

Questions regarding the code or the project can be asked to the team
by [raising an issue on the repo](https://github.com/navikt/soknadsmottaker/issues).

### For NAV employees

NAV employees can reach the team by Slack in the channel #team-fyllut-sendinn
