package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.soknad.arkivering.dto.InputTransformer
import no.nav.soknad.arkivering.dto.SoknadInnsendtDto
import no.nav.soknad.arkivering.dto.SoknadMottattDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ArchiverService(private val kafkaSender: KafkaSender) {
	private val logger = LoggerFactory.getLogger(javaClass)

	fun archive(request: SoknadInnsendtDto) {
		val kafkaMessage = convertMessage(request)

		publishToKafka(kafkaMessage)
	}

	private fun convertMessage(request: SoknadInnsendtDto) = InputTransformer(request).apply()

	private fun publishToKafka(data: SoknadMottattDto) {
		logger.info("Publishing to Kafka: $data")
		kafkaSender.publish("privat-soknadInnsendt-sendsoknad-v1-q0", "personId", data)
	}
}
