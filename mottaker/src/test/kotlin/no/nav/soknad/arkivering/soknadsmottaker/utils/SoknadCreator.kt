package no.nav.soknad.arkivering.soknadsmottaker.utils

import no.nav.soknad.arkivering.soknadsmottaker.model.AvsenderDto
import no.nav.soknad.arkivering.soknadsmottaker.model.BrukerDto
import no.nav.soknad.arkivering.soknadsmottaker.model.DokumentData
import no.nav.soknad.arkivering.soknadsmottaker.model.Innsending
import no.nav.soknad.arkivering.soknadsmottaker.model.Variant
import java.time.OffsetDateTime
import java.util.UUID


fun createInnsending(
	tema: String = "HJE", skjemanr: String = "NAV 10-07.54", tittel: String = "Søknad om servicehund",
	vedlegg: List<DokumentData> = createDefaultDokumentListe(),
	brukerDto: BrukerDto?,
	avsenderDto: AvsenderDto= AvsenderDto(id = "01234567891", idType = AvsenderDto.IdType.FNR, navn = null),
	kanal: String = "NAV_NO",
	innsendtDato: OffsetDateTime? = OffsetDateTime.now(),
	ettersendelseTilId: String? = null) = Innsending (
	innsendingsId = UUID.randomUUID().toString(),
	kanal = kanal,
	avsenderDto = avsenderDto,
	brukerDto = brukerDto,
	tema = tema,
	skjemanr = skjemanr,
	tittel = tittel,
	dokumenter = vedlegg,
	innsendtDato = innsendtDato,
	ettersendelseTilId = ettersendelseTilId
)

fun createDefaultDokumentListe() : List<DokumentData> {
	return listOf(
		createHovedkumentDokument("NAV 10-07.54", "Søknad om servicehund"),
		createKvitteringDokument(),
		createVedleggDokument()
	)
}

fun createKvitteringDokument(): DokumentData {
	return DokumentData(
		skjemanummer = "L7",
		erHovedskjema = false,
		tittel = "Kvittering",
		varianter = listOf(createKvitteringVariant())
	)
}

fun createHovedkumentDokument(skjemanr: String, tittel: String): DokumentData {
	return DokumentData(
		skjemanummer = skjemanr,
		erHovedskjema = true,
		tittel = tittel,
		varianter = createHovedkumentVariant()
	)
}

fun createKvitteringVariant(): Variant {
	return Variant(
		uuid = "e7179251-635e-493a-948c-749a39eedacc",
		mediaType = "application/pdf",
		filnavn = "kvittering.pdf",
		filtype = "PDFA",
		variantFormat	= "ARKIV"
	)
}

fun createHovedkumentVariant(): List<Variant> {
	return listOf(
		Variant(
		uuid = "e7179251-635e-493a-948c-749a39eedacc",
		mediaType = "application/pdf",
		filnavn = "kvittering.pdf",
		filtype = "PDFA",
		variantFormat	= "ARKIV"
	),
		Variant(
			uuid = "e7179251-635e-493a-948c-749a39eedacc",
			mediaType = "application/json",
			filnavn = "kvittering.json",
			filtype = "JSON",
			variantFormat	= "ORIGINAL"
		)
	)
}

fun createVedleggDokument(skjemanr: String = "L8", tittel: String = "Vedlegg"): DokumentData {
	return DokumentData(
		skjemanummer = skjemanr,
		erHovedskjema = false,
		tittel = tittel,
		varianter = listOf(createVedleggVariant(skjemanr))
	)
}

fun createVedleggVariant(skjemanr: String = "L8"): Variant  {
		return Variant(
			uuid = "e7179251-635e-493a-948c-749a39eedacc",
			mediaType = "application/pdf",
			filnavn = skjemanr+".pdf",
			filtype = "PDFA",
			variantFormat = "ARKIV"
		)
}

private val defaultSkjemanr = "NAV 10-07.54"

