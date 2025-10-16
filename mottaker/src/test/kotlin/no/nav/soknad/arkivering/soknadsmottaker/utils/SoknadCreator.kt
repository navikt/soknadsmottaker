package no.nav.soknad.arkivering.soknadsmottaker.utils

import no.nav.soknad.arkivering.soknadsmottaker.model.AvsenderDto
import no.nav.soknad.arkivering.soknadsmottaker.model.BrukerDto
import no.nav.soknad.arkivering.soknadsmottaker.model.DocumentData
import no.nav.soknad.arkivering.soknadsmottaker.model.DokumentData
import no.nav.soknad.arkivering.soknadsmottaker.model.Innsending
import no.nav.soknad.arkivering.soknadsmottaker.model.Soknad
import no.nav.soknad.arkivering.soknadsmottaker.model.Variant
import no.nav.soknad.arkivering.soknadsmottaker.model.Varianter
import java.util.UUID

fun createSoknad() = Soknad(
	"17e10f63-443c-4aba-829b-d598c3a74248",
	false,
	"01234567891",
	"BIL",
	createDocuments()
)

fun createDocuments(variants: List<Varianter> = createVariants()) = listOf(
	DocumentData(
		"NAV 10-07.40",
		true,
		"Søknad om stønad til anskaffelse av motorkjøretøy",
		variants
	)
)

fun createVariants() = listOf(createVariant())

fun createVariant(mediaType: String = "application/pdf") = Varianter(
	id = "e7179251-635e-493a-948c-749a39eedacc",
	mediaType = mediaType,
	filnavn = "innsending.pdf",
	filtype = "PDFA",
)

fun createInnsending(
	tema: String = "HJE", skjemanr: String = "NAV 10-07.54", tittel: String = "Søknad om servicehund",
	vedlegg: List<DokumentData> = createDefaultDokumentListe() , brukerDto: BrukerDto?) = Innsending (
	innsendingsId = UUID.randomUUID().toString(),
	kanal = "NOLOGIN_NAV_NO",
	avsenderDto = AvsenderDto(id = "01234567891", idType = AvsenderDto.IdType.FNR),
	brukerDto = brukerDto,
	tema = tema,
	skjemanr = skjemanr,
	tittel = tittel,
	dokumenter = vedlegg
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
		skjemanummer = "NAV 10-07.54",
		erHovedskjema = true,
		tittel = "Søknad om servicehund",
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
