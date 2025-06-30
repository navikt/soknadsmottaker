package no.nav.soknad.arkivering.soknadsmottaker.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.soknad.arkivering.avroschemas.InnsendingMetrics
import no.nav.soknad.arkivering.soknadsmottaker.model.Innsending
import no.nav.soknad.arkivering.soknadsmottaker.model.InnsendingTopicMsg
import no.nav.soknad.arkivering.soknadsmottaker.supervision.InnsendtMetrics
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class InnsendingService( private val kafkaSender: KafkaSender, private val metrics: InnsendtMetrics) {

	private val logger = LoggerFactory.getLogger(javaClass)

	fun publishToNoLoginTopic(key: String, innsending: Innsending) {
		val msg = mapTilInnsendingTopicMsg(innsending, false)

		val startTime = System.currentTimeMillis()
		try {
			kafkaSender.publishNologinSubmission(key, msg)
			logger.info("$key: Published to NoLogintopic. skjemanr: ${innsending.skjemanr}")
		} catch (error: Exception) {
			logger.error("$key: Error publishing to NoLoginTopic. skjemanr: ${innsending.skjemanr}", error)
			metrics.mottattErrorInc(innsending.tema)
			throw error
		} finally {
			tryPublishingMetrics(key, startTime)
		}

	}

	private fun mapTilInnsendingTopicMsg(innsending: Innsending, erInnlogget: Boolean): String {
		val msg = InnsendingTopicMsg(
			innsendtDato = OffsetDateTime.now(),
			innlogget = erInnlogget,
			innsendingsId = innsending.innsendingsId,
			ettersendelseTilId = innsending.ettersendelseTilId,
			avsenderDto = innsending.avsenderDto,
			brukerDto = innsending.brukerDto,
			kanal = innsending.kanal,
			skjemanr = innsending.tittel,
			tittel = innsending.tittel,
			arkivtema = innsending.tema,
			dokumenter = innsending.dokumenter
		)
		val mapper = jacksonObjectMapper()
		mapper.findAndRegisterModules()
		return mapper.writeValueAsString(msg)

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
