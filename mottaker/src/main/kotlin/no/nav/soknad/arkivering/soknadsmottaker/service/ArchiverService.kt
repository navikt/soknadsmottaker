package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.soknad.arkivering.avroschemas.InnsendingMetrics
import no.nav.soknad.arkivering.avroschemas.Soknadarkivschema
import no.nav.soknad.arkivering.soknadsmottaker.dto.InputTransformer
import no.nav.soknad.arkivering.soknadsmottaker.dto.SoknadInnsendtDto
import no.nav.soknad.arkivering.soknadsmottaker.supervision.InnsendtMetrics
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ArchiverService(
	private val kafkaSender: KafkaSender,
	private val metrics: InnsendtMetrics
) {

	private val logger = LoggerFactory.getLogger(javaClass)

	fun archive(key: String, request: SoknadInnsendtDto) {
		val startTime = System.currentTimeMillis()
		try {

			val kafkaMessage = convertMessage(request)
			publishToKafka(key, kafkaMessage)

			metrics.mottattSoknadInc(request.tema)
		} catch (error: Exception) {
			metrics.mottattErrorInc(request.tema)
			throw error
		} finally {
			tryPublishingMetrics(key, startTime)
		}
	}

	private fun convertMessage(request: SoknadInnsendtDto) = InputTransformer(request).apply()

	private fun publishToKafka(key: String, data: Soknadarkivschema) {
		try {
			kafkaSender.publish(key, data)
			logger.info("$key: Published Soknadarkivschema. Key: '$key'. MeldingId '${data.behandlingsid}'")

		} catch (t: Throwable) {
			logger.error("$key: Failed to publish Soknadarkivschema. Key: '$key'. MeldingId '${data.behandlingsid}'", t)
			throw t
		}
	}


	private fun tryPublishingMetrics(key: String, startTime: Long) {
		try {
			val duration = System.currentTimeMillis() - startTime

			val innsendingMetrics = InnsendingMetrics("soknadsmottaker", "publish to kafka", startTime, duration)
			kafkaSender.publishMetric(key, innsendingMetrics)
		} catch (e: Exception) {
			logger.error("$key: Caught exception when publishing metric", e)
		}
	}
}
