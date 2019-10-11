package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.soknad.arkivering.dto.SoknadInnsendtDto
import no.nav.soknad.arkivering.dto.SoknadMottattDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ArchiverService(private val kafkaSender: KafkaSender) {
	private val logger = LoggerFactory.getLogger(javaClass)

	fun archive(message: SoknadInnsendtDto) {
		val mottattSoknad = message.toSoknadMottattView()
		publishToKafka(mottattSoknad)
	}
	private fun SoknadInnsendtDto.toSoknadMottattView() = SoknadMottattDto(
		henvendelsesId = henvendelsesId,
		ettersendelsesId = ettersendelsesId,
		personId = personId,
		tema = tema,
		innsendtDato = innsendtDato,
		mottatteDokumenter = innsendteDokumenter
	)

	private fun publishToKafka(archivalData: SoknadMottattDto) {
		logger.info("Publishing to Kafka: $archivalData")
		kafkaSender.publish("privat-soknadInnsendt-sendsoknad-v1-q0", "personId", archivalData)
	}
}
