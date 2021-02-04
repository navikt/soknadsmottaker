package no.nav.soknad.arkivering.soknadsmottaker.service

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import no.nav.soknad.arkivering.avroschemas.InnsendingMetrics
import no.nav.soknad.arkivering.avroschemas.Soknadarkivschema
import no.nav.soknad.arkivering.soknadsmottaker.config.AppConfiguration
import no.nav.soknad.arkivering.soknadsmottaker.dto.InputTransformer
import no.nav.soknad.arkivering.soknadsmottaker.dto.SoknadInnsendtDto
import no.nav.soknad.arkivering.soknadsmottaker.supervise.InnsendtMetrics
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
			GlobalScope.launch {
				kafkaSender.publishMetric(
					appConfiguration.kafkaConfig.metricsTopic, key,
					InnsendingMetrics("soknadsmottaker", "publish to kafka", startTime, System.currentTimeMillis() - startTime)
				)
			}
		}
	}

	private fun convertMessage(request: SoknadInnsendtDto) = InputTransformer(request).apply()

	private fun publishToKafka(data: Soknadarkivschema, key: String) {
		logger.info("Publishing to topic '$topic'. Key: '$key'. MeldingId '${data.behandlingsid}'")

		kafkaSender.publish(topic, key, data)

		logger.info("Published to topic '$topic'. Key: '$key'. MeldingId '${data.behandlingsid}'")
	}
}
