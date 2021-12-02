# Soknadsmottaker
When a user applies for a benefit (_sender inn en søknad_), one or more documents are sent in to NAV. This component acts as a REST-endpoint to which the systems that the user uses can send metadata about the benefit. The documents themselves are sent to a different system, [Soknadsfillager](https://www.github.com/navikt/soknadsfillager).

When Soknadsmottaker receives data, it will be converted, serialized as an Avro message and put on a Kafka topic.

For a description of the whole archiving system, see [the documentation](https://github.com/navikt/archiving-infrastructure/wiki).

# Dependencies
This component requires the following to work:
* [soknadarkiv-schema](https://github.com/navikt/soknadarkiv-schema) (Avro schema definitions)
* Kafka broker (for providing Kafka topics to send to)
* Shared secrets on Vault.


## Inquiries
Questions regarding the code or the project can be asked to [team-soknad@nav.no](mailto:team-soknad@nav.no)

### For NAV employees
NAV employees can reach the team by Slack in the channel #teamsoknad
