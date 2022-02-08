package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.soknad.arkivering.avroschemas.MottattDokument
import no.nav.soknad.arkivering.avroschemas.MottattVariant
import no.nav.soknad.arkivering.avroschemas.Soknadarkivschema
import no.nav.soknad.arkivering.avroschemas.Soknadstyper
import no.nav.soknad.arkivering.soknadsmottaker.model.DocumentData
import no.nav.soknad.arkivering.soknadsmottaker.model.Soknad
import no.nav.soknad.arkivering.soknadsmottaker.model.Varianter
import java.time.LocalDateTime
import java.time.ZoneOffset

fun convert(soknad: Soknad) = Soknadarkivschema(
	soknad.innsendingId,
	soknad.personId,
	soknad.tema,
	LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
	if (soknad.erEttersendelse) Soknadstyper.ETTERSENDING else Soknadstyper.SOKNAD,
	convertDocuments(soknad.dokumenter)
)

fun convertDocuments(list: List<DocumentData>) = list.map { convertDocument(it) }
fun convertDocument(document: DocumentData) = MottattDokument(
	document.skjemanummer,
	document.erHovedskjema,
	document.tittel,
	convertVarianter(document.varianter, document.erHovedskjema)
)

fun convertVarianter(list: List<Varianter>, erHovedskjema: Boolean) = list.map { convertVariant(it, erHovedskjema) }
fun convertVariant(varianter: Varianter, erHovedskjema: Boolean) = MottattVariant(
	varianter.id,
	varianter.filnavn,
	varianter.filtype,
	getVariantformat(varianter.mediaType, erHovedskjema)
)

private fun getVariantformat(mediaType: String, erHovedskjema: Boolean): String {
	return if (erHovedskjema)
		"ARKIV"
	else if ("application/json".equals(mediaType, ignoreCase = true) || "application/xml".equals(mediaType, ignoreCase = true))
		"ORIGINAL"
	else
		"FULLVERSJON"
}
