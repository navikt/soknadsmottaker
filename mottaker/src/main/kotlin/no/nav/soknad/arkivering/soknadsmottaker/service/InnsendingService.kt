package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.soknad.arkivering.avroschemas.InnsendingMetrics
import no.nav.soknad.arkivering.soknadsmottaker.model.Innsending
import no.nav.soknad.arkivering.soknadsmottaker.supervision.InnsendtMetrics
import no.nav.soknad.arkivering.soknadsmottaker.supervision.MetricNames
import no.nav.soknad.arkivering.soknadsmottaker.util.mapTilInnsendingTopicMsg
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class InnsendingService( private val kafkaSender: KafkaSender, private val metrics: InnsendtMetrics) {

	private val logger = LoggerFactory.getLogger(javaClass)

	fun publishToNoLoginTopic(key: String, innsending: Innsending) {
		val msg = mapTilInnsendingTopicMsg(innsending, false)

		val startTime = System.currentTimeMillis()
		try {
			kafkaSender.publishNologinSubmission(key, msg)
			logger.info("$key: Published to NoLogintopic. skjemanr: ${innsending.skjemanr}")
			metrics.mottattSoknadInc(MetricNames.INNSENDT_UINNLOGGET.name, innsending.tema)
		} catch (error: Exception) {
			logger.error("$key: Error publishing to NoLoginTopic. skjemanr: ${innsending.skjemanr}", error)
			metrics.mottattSoknadInc(MetricNames.INNSENDT_UINNLOGGET_ERROR.name, innsending.tema)
			throw error
		} finally {
			tryPublishingMetrics(key, startTime)
		}

	}

	private fun tryPublishingMetrics(key: String, startTime: Long) {
		try {
			val duration = System.currentTimeMillis() - startTime

			val metric = InnsendingMetrics("soknadsmottaker", "publish to kafka", startTime, duration)
			kafkaSender.publishMetric(key, metric)
		} catch (e: Exception) {
			logger.error("$key: Caught exception when publishing metric", e)
		}
	}

}
