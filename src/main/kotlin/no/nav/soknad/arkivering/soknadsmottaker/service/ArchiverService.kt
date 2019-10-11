package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.soknad.arkivering.dto.InnsendtDokumentDto
import no.nav.soknad.arkivering.dto.MottattDokumentDto
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
		mottatteDokumenter = konverterTilMottatteDokumenterList(innsendteDokumenter)
	)

	private fun InnsendtDokumentDto.toMottattDokumentView() = MottattDokumentDto(
		uuid = uuid,
		erAlternativRepresentasjon = erAlternativRepresentasjon,
		erHovedSkjema =  erHovedSkjema,
		skjemaNummer = skjemaNummer,
		tittel = tittel,
		filNavn = filNavn,
		filStorrelse = filStorrelse,
		mimeType = mimeType
	)

	private fun konverterTilMottatteDokumenterList(innsendtDto: List<InnsendtDokumentDto>): List<MottattDokumentDto> {
		return innsendtDto
			.map{ f -> f.toMottattDokumentView()}
			.toList()
	}

	private fun publishToKafka(archivalData: SoknadMottattDto) {
		logger.info("Publishing to Kafka: $archivalData")
		kafkaSender.publish("privat-soknadInnsendt-sendsoknad-v1-q0", "personId", archivalData)
	}
}
