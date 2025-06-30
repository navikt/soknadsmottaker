package no.nav.soknad.arkivering.soknadsmottaker.util

import no.nav.soknad.arkivering.soknadsmottaker.model.AvsenderDto
import no.nav.soknad.arkivering.soknadsmottaker.model.BrukerDto
import no.nav.soknad.arkivering.soknadsmottaker.model.DokumentData
import no.nav.soknad.arkivering.soknadsmottaker.model.Innsending

fun maskDokumentTitle(documents:List<DokumentData>): List<DokumentData> {
	return documents.map{DokumentData(it.skjemanummer, it.erHovedskjema, if (it.skjemanummer == "N6") "**Maskert**" else it.tittel, it.varianter)}
}

fun maskIdsInInnsending(soknad: Innsending) = Innsending(
		innsendingsId = soknad.innsendingsId,
		ettersendelseTilId = soknad.ettersendelseTilId,
		avsenderDto = AvsenderDto(
			id = "**id can be found in secure logs**",
			idType = soknad.avsenderDto.idType,
			navn = "**navn can be found in secure logs**"
		),
		brukerDto = if (soknad.brukerDto != null) BrukerDto(id = "**id can be found in secure logs**", idType = soknad.brukerDto!!.idType) else null,
		kanal = soknad.kanal,
		tema = soknad.tema,
		skjemanr = soknad.skjemanr,
		tittel = soknad.tittel,
		dokumenter = maskDokumentTitle(soknad.dokumenter)
	)
