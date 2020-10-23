package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.soknad.arkivering.avroschemas.Soknadarkivschema
import no.nav.soknad.arkivering.soknadsmottaker.supervise.Metrics
import no.nav.soknad.arkivering.soknadsmottaker.config.AppConfiguration
import no.nav.soknad.arkivering.soknadsmottaker.dto.InputTransformer
import no.nav.soknad.arkivering.soknadsmottaker.dto.SoknadInnsendtDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class ArchiverService(private val kafkaSender: KafkaSender, appConfiguration: AppConfiguration) {
	private val logger = LoggerFactory.getLogger(javaClass)
	private val topic = appConfiguration.kafkaConfig.topic

	fun archive(request: SoknadInnsendtDto) {
		try {
			val kafkaMessage = convertMessage(request)
			publishToKafka(kafkaMessage)

			Metrics.mottattSoknadInc(request.tema)
		} catch (error: Exception) {
			Metrics.mottattErrorInc(request.tema)
			throw error
		}
	}

	private fun convertMessage(request: SoknadInnsendtDto) = InputTransformer(request).apply()

	private fun publishToKafka(data: Soknadarkivschema) {
		val key = UUID.randomUUID().toString()
		logger.info("Publishing to topic '$topic'. Key: '$key'. MeldingId '${data.getBehandlingsid()}'")

		kafkaSender.publish(topic, key, data)

		logger.info("Published to topic '$topic'. Key: '$key'. MeldingId '${data.getBehandlingsid()}'")
	}
}
