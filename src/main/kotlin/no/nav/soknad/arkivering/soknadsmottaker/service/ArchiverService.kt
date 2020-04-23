package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.soknad.arkivering.soknadsmottaker.config.AppConfiguration
import no.nav.soknad.arkivering.soknadsmottaker.dto.InputTransformer
import no.nav.soknad.arkivering.soknadsmottaker.dto.SoknadInnsendtDto
import no.nav.soknad.soknadarkivering.avroschemas.Soknadarkivschema
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ArchiverService(private val kafkaSender: KafkaSender) {
	private val logger = LoggerFactory.getLogger(javaClass)
	private val topic = AppConfiguration().kafkaConfig.topic

	fun archive(request: SoknadInnsendtDto) {
		val kafkaMessage = convertMessage(request)

		publishToKafka(kafkaMessage)
	}

	private fun convertMessage(request: SoknadInnsendtDto) = InputTransformer(request).apply()

	private fun publishToKafka(data: Soknadarkivschema) {
		logger.info("Publishing to Kafka: $data")

		kafkaSender.publish(topic, "personId", data)
	}
}
