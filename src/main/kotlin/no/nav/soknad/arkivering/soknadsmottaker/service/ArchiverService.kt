package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.soknad.arkivering.dto.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ArchiverService(private val kafkaSender: KafkaSender) {
	private val logger = LoggerFactory.getLogger(javaClass)

	fun archive(message: SoknadInnsendtDto) {
		val mottattSoknad = message.toSoknadMottattView()
		publishToKafka(mottattSoknad)
	}
// mappe til https://dokarkiv-q1.nais.preprod.local/swagger-ui.html#/arkiver-og-journalfoer-rest-controller/opprettJournalpostUsingPOST
	private fun SoknadInnsendtDto.toSoknadMottattView() = SoknadMottattDto(
	eksternReferanseId= innsendingsId,
		personId = personId,
		tema = tema,
		innsendtDato = innsendtDato,
		mottatteDokumenter = konverterTilMottatteDokumenterList(innsendteDokumenter)
	)

	private fun InnsendtDokumentDto.toMottattDokumentView() = MottattDokumentDto(
		skjemaNummer = skjemaNummer,
		erHovedSkjema =  erHovedSkjema,
		tittel = tittel,
		varianter =  konverterTilMotattVarianterListe(varianter)
	)

	private fun InnsendtVariantDto.toMottattVariantView() = MottattVariantDto(
		uuid = uuid,
		filNavn = filNavn,
		filtype = filtype,
		variantformat = variantformat
	)
			private fun konverterTilMotattVarianterListe(innsendtVariantDto: List<InnsendtVariantDto>): List<MottattVariantDto> {
			return innsendtVariantDto
				.map { f -> f.toMottattVariantView() }
				.toList()
		}

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
