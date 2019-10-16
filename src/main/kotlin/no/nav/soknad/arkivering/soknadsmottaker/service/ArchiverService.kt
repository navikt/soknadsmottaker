package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.soknad.arkivering.dto.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ArchiverService(private val kafkaSender: KafkaSender) {
	private val logger = LoggerFactory.getLogger(javaClass)

	fun archive(message: SoknadMottattDto) {

		publishToKafka(message)
	}
// mappe til https://dokarkiv-q1.nais.preprod.local/swagger-ui.html#/arkiver-og-journalfoer-rest-controller/opprettJournalpostUsingPOST


	private fun publishToKafka(archivalData: SoknadMottattDto) {
		logger.info("Publishing to Kafka: $archivalData")
		kafkaSender.publish("privat-soknadInnsendt-sendsoknad-v1-q0", "personId", archivalData)
	}
}
