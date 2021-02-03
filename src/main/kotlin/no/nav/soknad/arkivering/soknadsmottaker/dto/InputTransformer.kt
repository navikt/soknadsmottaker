package no.nav.soknad.arkivering.soknadsmottaker.dto

import no.nav.soknad.arkivering.avroschemas.MottattDokument
import no.nav.soknad.arkivering.avroschemas.MottattVariant
import no.nav.soknad.arkivering.avroschemas.Soknadarkivschema
import no.nav.soknad.arkivering.avroschemas.Soknadstyper
import java.time.ZoneOffset

class InputTransformer(private val input: SoknadInnsendtDto) {

	fun apply(): Soknadarkivschema = input.toSoknadMottattView()

	private fun SoknadInnsendtDto.toSoknadMottattView() = Soknadarkivschema(innsendingsId, personId, tema,
		innsendtDato.toEpochSecond(ZoneOffset.UTC), if (ettersendelse) Soknadstyper.ETTERSENDING else Soknadstyper.SOKNAD,
		konverterTilMottatteDokumenterList(innsendteDokumenter)
	)

	private fun InnsendtDokumentDto.toMottattDokumentView() = MottattDokument(skjemaNummer, erHovedSkjema, tittel ?: "Ikke tilgjengelig",
		konverterTilMotattVarianterListe(varianter))

	private fun InnsendtVariantDto.toMottattVariantView() = MottattVariant(uuid, filNavn ?: "Ikke tilgjengelig", filtype, variantformat)

	private fun konverterTilMotattVarianterListe(list: List<InnsendtVariantDto>) = list.map { it.toMottattVariantView() }

	private fun konverterTilMottatteDokumenterList(list: List<InnsendtDokumentDto>) = list.map { it.toMottattDokumentView() }
}
