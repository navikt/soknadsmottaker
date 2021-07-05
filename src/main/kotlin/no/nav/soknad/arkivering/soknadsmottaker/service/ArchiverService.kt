package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.soknad.arkivering.avroschemas.InnsendingMetrics
import no.nav.soknad.arkivering.avroschemas.Soknadarkivschema
import no.nav.soknad.arkivering.soknadsmottaker.config.AppConfiguration
import no.nav.soknad.arkivering.soknadsmottaker.dto.InputTransformer
import no.nav.soknad.arkivering.soknadsmottaker.dto.SoknadInnsendtDto
import no.nav.soknad.arkivering.soknadsmottaker.supervision.InnsendtMetrics
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class ArchiverService(
	private val kafkaSender: KafkaSender,
	private val appConfiguration: AppConfiguration,
	private val metrics: InnsendtMetrics
) {

	private val logger = LoggerFactory.getLogger(javaClass)
	private val topic = appConfiguration.kafkaConfig.topic

	fun archive(request: SoknadInnsendtDto) {
		val startTime = System.currentTimeMillis()
		val key = UUID.randomUUID().toString()
		try {

			val kafkaMessage = convertMessage(request)
			publishToKafka(kafkaMessage, key)

			metrics.mottattSoknadInc(request.tema)
		} catch (error: Exception) {
			metrics.mottattErrorInc(request.tema)
			throw error
		} finally {
			tryPublishingMetrics(key, startTime)
		}
	}

	private fun convertMessage(request: SoknadInnsendtDto) = InputTransformer(request).apply()

	private fun publishToKafka(data: Soknadarkivschema, key: String) {
		try {
			kafkaSender.publish(topic, key, data)
			logger.info("Published to topic '$topic'. Key: '$key'. MeldingId '${data.behandlingsid}'")

		} catch (t: Throwable) {
			logger.error("Failed to publish to topic '$topic'. Key: '$key'. MeldingId '${data.behandlingsid}'", t)
			throw t
		}
	}


	private fun tryPublishingMetrics(key: String, startTime: Long) {
		try {
			val duration = System.currentTimeMillis() - startTime

			kafkaSender.publishMetric(
				appConfiguration.kafkaConfig.metricsTopic, key,
				InnsendingMetrics("soknadsmottaker", "publish to kafka", startTime, duration)
			)
		} catch (e: Exception) {
			logger.error("Caught exception when publishing metric", e)
		}
	}
}
