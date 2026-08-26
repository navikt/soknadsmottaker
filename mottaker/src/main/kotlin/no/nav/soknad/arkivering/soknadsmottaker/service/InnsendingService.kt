package no.nav.soknad.arkivering.soknadsmottaker.service

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.soknad.arkivering.soknadsmottaker.model.Innsending
import no.nav.soknad.arkivering.soknadsmottaker.model.InnsendingMetrics
import no.nav.soknad.arkivering.soknadsmottaker.supervision.InnsendtMetrics
import no.nav.soknad.arkivering.soknadsmottaker.supervision.MetricNames
import no.nav.soknad.arkivering.soknadsmottaker.util.mapTilInnsendingTopicMsg
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Service
class InnsendingService(
	private val kafkaSender: KafkaSender,
	private val metrics: InnsendtMetrics,
	private val objectMapper: ObjectMapper) {

	private val logger = LoggerFactory.getLogger(javaClass)

	fun publishToNoLoginTopic(key: String, innsending: Innsending) {
		val startTime = OffsetDateTime.now(ZoneOffset.UTC)
		try {
			publishSubmission(key, innsending, isLoggedIn=false)
			logger.info("$key: Not logged in, published to kanal: ${innsending.kanal}, skjemanr: ${innsending.skjemanr}")
			metrics.mottattSoknadInc(MetricNames.INNSENDT_UINNLOGGET.name, innsending.tema)
		} catch (error: Exception) {
			logger.error("$key: Error publishing to NoLoginTopic. skjemanr: ${innsending.skjemanr}", error)
			metrics.mottattSoknadInc(MetricNames.INNSENDT_UINNLOGGET_ERROR.name, innsending.tema)
			throw error
		} finally {
			tryPublishingMetrics(key, startTime)
		}
	}


	fun publishToLoggedinTopic(key: String, innsending: Innsending) {
		val startTime = OffsetDateTime.now(ZoneOffset.UTC)
		try {
			publishSubmission(key, innsending, isLoggedIn=true)
			logger.info("$key: Logged in, published to kanal: ${innsending.kanal}, skjemanr: ${innsending.skjemanr}")
			metrics.mottattSoknadInc(MetricNames.INNSENDT_OK.name, innsending.tema)
		} catch (error: Exception) {
			logger.error("$key: Error publishing to LoggedinTopic. skjemanr: ${innsending.skjemanr}", error)
			metrics.mottattSoknadInc(MetricNames.INNSENDT_ERROR.name, innsending.tema)
			throw error
		} finally {
			tryPublishingMetrics(key, startTime)
		}
	}

	fun publishSubmission(key: String, value: Innsending, isLoggedIn: Boolean) {
		logger.info("$key: shall publish submission to kanal ${value.kanal}")
		val valueAsString = objectMapper.writeValueAsString(mapTilInnsendingTopicMsg(value, isLoggedIn))
		kafkaSender.publishSubmission( key, valueAsString, isLoggedIn)
	}

	private fun tryPublishingMetrics(key: String, startTime: OffsetDateTime) {
		try {
			val duration = Duration.between(startTime, OffsetDateTime.now(ZoneOffset.UTC)).toMillis()

			val metric = InnsendingMetrics("soknadsmottaker", "publish to kafka", startTime, duration)
			kafkaSender.publishMetric(key, metric)
		} catch (e: Exception) {
			logger.error("$key: Caught exception when publishing metric", e)
		}
	}

}
