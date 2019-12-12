package no.nav.soknad.arkivering.dto

import java.time.LocalDateTime

//forsendelse informasjon
private const val innsendingsidIdForBilForsendelse = "IS123456"
private var erEttersendelseBil = false
private const val personIDBil = "12345678910"
private const val temaBil = "BIL"
private val innsendtDatoBil = LocalDateTime.now()
// hovedskjema
const val skjemanummerBil = "NAV 10-07.40"
private const val erHovedSkjemaBil = true
const val tittelBil = "Søknad om stønad til anskaffelse av motorkjøretøy"
const val uuidBil = "e7179251-635e-493a-948c-749a39eedacc"
const val filNavnBil = "skjemanummerBil + filformat burde den vært"
private const val filStorrelseBil = "10000"
const val variantformatBilHovedskjema = "ARKIV"
const val filtypeBilHoveskjema = "PDFA"
//Vedlegg
//Kvittering
const val uuidBilKvittering = "e7179251-635e-493a-948c-749a39kviter"
const val filnavnKvitteering = "kvittering.pdf"
private const val tittelKvitteering = "kvittering"
private const val skjemanummerKvittering = "L7"
const val variantformatBilKvittering = "ARKIV"
const val filtypeBilKvittering = "PDFA"
// forerkort
private const val uuidBILvedlegg = "e7179251-635e-493a-948c-749a39vedleg"
private const val filnavnForerkort = "forerkort.pdf"
private const val tittelForerkort = "Kopi av førerkort"
private const val skjemanummerForerkort = "Z4"
private const val filtypeBilForerkort = "PDF"
// felles for vedlegg
private const val filstorrelseVedlegg = "101010"
private const val erIkkeHovedskjemaBil = false
private const val variantformatOrginal = "ORGINAL"
private const val mimeTypeBil = "er det bruk for denne? bør vel være dokumenttype" // pdf, xml, json, pdfa


fun opprettBilInnsendingMedBareSoknadOgKvittering(): SoknadInnsendtDto {
	val soknad: InnsendtVariantDto = opprettHoveddokumentVariant()
	val kvittering: InnsendtVariantDto = opprettKvitteringVariant()
	val soknadsDokument: InnsendtDokumentDto = innsendtHovedskjemaDokument(soknad)
	val kvitteringDokument: InnsendtDokumentDto = innsendtKvitteringDokument(kvittering)

	val listeAvDokumenter = mutableListOf(kvitteringDokument, soknadsDokument)
	return SoknadInnsendtDto(innsendingsidIdForBilForsendelse, erEttersendelseBil, personIDBil, temaBil, innsendtDatoBil, innsendteDokumenter = listeAvDokumenter)
}

private fun opprettForerkortVedleggMedVariantOgDokument(): InnsendtDokumentDto {
	val forerkortVariant = opprettForerkortSomVedleggVariant()
	return InnsendtDokumentDto(skjemanummerForerkort, false, tittelForerkort, mutableListOf(forerkortVariant))
}

fun opprettForerkortSomVedleggVariant() =
	InnsendtVariantDto(uuidBILvedlegg, mimeTypeBil, filnavnForerkort, filstorrelseVedlegg, variantformatOrginal, filtypeBilForerkort)

private fun opprettKvitteringVariant() =
	InnsendtVariantDto(uuidBilKvittering, mimeTypeBil, filnavnKvitteering, filstorrelseVedlegg, variantformatBilKvittering, filtypeBilKvittering)

fun opprettHoveddokumentVariant() =
	InnsendtVariantDto(uuidBil, mimeTypeBil, filNavnBil, filStorrelseBil, variantformatBilHovedskjema, filtypeBilHoveskjema)

fun innsendtHovedskjemaDokument(variantHovedskjemaBil: InnsendtVariantDto) =
	InnsendtDokumentDto(skjemanummerBil, erHovedSkjemaBil, tittelBil, listOf(variantHovedskjemaBil))

private fun innsendtKvitteringDokument(variantKvitteringBil: InnsendtVariantDto)=
	InnsendtDokumentDto(skjemanummerKvittering, erIkkeHovedskjemaBil, tittelKvitteering, listOf(variantKvitteringBil))