package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.soknad.arkivering.avroschemas.MottattDokument
import no.nav.soknad.arkivering.avroschemas.MottattVariant
import no.nav.soknad.arkivering.avroschemas.Soknadarkivschema
import no.nav.soknad.arkivering.avroschemas.Soknadstyper
import no.nav.soknad.arkivering.soknadsmottaker.model.DocumentData
import no.nav.soknad.arkivering.soknadsmottaker.model.Soknad
import no.nav.soknad.arkivering.soknadsmottaker.model.Varianter
import java.time.OffsetDateTime

fun convert(soknad: Soknad) = Soknadarkivschema(
	soknad.innsendingId,
	soknad.personId,
	soknad.tema,
	OffsetDateTime.now().toEpochSecond(),
	if (soknad.erEttersendelse) Soknadstyper.ETTERSENDING else Soknadstyper.SOKNAD,
	convertDocuments(soknad.dokumenter)
)

fun convertDocuments(list: List<DocumentData>) = list.map { convertDocument(it) }
fun convertDocument(document: DocumentData) = MottattDokument(
	document.skjemanummer,
	document.erHovedskjema,
	document.tittel,
	convertVarianter(document.varianter)
)

fun convertVarianter(list: List<Varianter>) = list.map { convertVariant(it) }
fun convertVariant(varianter: Varianter) = MottattVariant(
	varianter.id,
	varianter.filnavn,
	varianter.filtype,
	getVariantformat(varianter.mediaType)
)

private fun getVariantformat(mediaType: String): String {
	return if ("application/pdf".equals(mediaType, ignoreCase = true))
		"ARKIV"
	else if ("application/pdf-fullversjon".equals(mediaType, ignoreCase = true))
		"FULLVERSJON"
	else
		"ORIGINAL"
}


