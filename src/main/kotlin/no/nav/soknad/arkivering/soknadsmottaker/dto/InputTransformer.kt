package no.nav.soknad.arkivering.soknadsmottaker.dto

import no.nav.soknad.soknadarkivering.avroschemas.MottattDokument
import no.nav.soknad.soknadarkivering.avroschemas.MottattVariant
import no.nav.soknad.soknadarkivering.avroschemas.Soknadarkivschema
import java.time.ZoneOffset

class InputTransformer(private val input: SoknadInnsendtDto) {

	fun apply(): Soknadarkivschema = input.toSoknadMottattView()

	private fun SoknadInnsendtDto.toSoknadMottattView() = Soknadarkivschema(innsendingsId, personId, tema, "",
		innsendtDato.toEpochSecond(ZoneOffset.UTC), konverterTilMottatteDokumenterList(innsendteDokumenter)
	)

	private fun InnsendtDokumentDto.toMottattDokumentView() = MottattDokument(skjemaNummer, erHovedSkjema, tittel,
		konverterTilMotattVarianterListe(varianter))

	private fun InnsendtVariantDto.toMottattVariantView() = MottattVariant(uuid, filNavn, filtype, variantformat)

	private fun konverterTilMotattVarianterListe(list: List<InnsendtVariantDto>) = list.map { it.toMottattVariantView() }

	private fun konverterTilMottatteDokumenterList(list: List<InnsendtDokumentDto>) = list.map { it.toMottattDokumentView() }
}
