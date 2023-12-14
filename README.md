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

The application will log the requests it receives, but mask the personId / fødselsnummer / fnr. For debugging and
resending purposes, the fnr is needed. It can be found in the secure logs of the application, accessible via Kibana or
Kubectl.

### Kibana

The secure logs can be accessed from [Kibana](https://logs.adeo.no/) under the index "Securelogs". If you lack access,
it is most likely because you are not in the AD-group.
See [the nais documentation](https://doc.nais.io/observability/logs/#gaining-access-in-kibana) for
details.

### Kubectl

The secure logs can be accessed from the terminal using kubectl as shown below. Remember to look in all pods for the log
line that you search for.

```
$ kubectl -n team-soknad get pods | grep soknadsmottaker
soknadsmottaker-86745fb779-lndkn    3/3    Running    0    2d18h
soknadsmottaker-86745fb779-r7ffk    3/3    Running    0    2d18h

$ kubectl -n team-soknad exec --stdin --tty soknadsmottaker-86745fb779-lndkn -- /bin/bash
Defaulting container name to soknadsmottaker.
Use 'kubectl describe pod/soknadsmottaker-86745fb779-lndkn -n team-soknad' to see all of the containers in this pod.

apprunner@soknadsmottaker-86745fb779-lndkn:/app$ ls -la /secure-logs/
total 14344
drwxrwxrwx. 2 root      root          4096 feb.  22 15:35 .
drwxr-xr-x. 1 root      root          4096 feb.  22 15:35 ..
-rw-r--r--. 1 apprunner apprunner 14666703 feb.  25 10:17 secure.log
-rw-r--r--. 1      1065      1065        0 feb.  22 15:35 secure-logs-mlog.pos
-rw-r--r--. 1      1065      1065       58 feb.  25 10:17 secure-logs.pos

apprunner@soknadsmottaker-86745fb779-lndkn:/app$ cat /secure-logs/secure.log
<LOG CONTENT>
```

## Inquiries

Questions regarding the code or the project can be asked to the team
by [raising an issue on the repo](https://github.com/navikt/soknadsmottaker/issues).

### For NAV employees

NAV employees can reach the team by Slack in the channel #team-fyllut-sendinn
