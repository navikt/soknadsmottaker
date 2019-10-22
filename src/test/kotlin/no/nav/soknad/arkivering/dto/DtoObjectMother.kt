package no.nav.soknad.arkivering.dto

import java.time.LocalDateTime

//forsendelse informasjon
private val innsendingsidIdForBilForsendelse = "IS123456"
private var erEttersendelseBil = false
private val personIDBil = "12345678910"
private val temaBil = "BIL"
private val innsendtDatoBil = LocalDateTime.now()
// hovedskjema
private val skjemanummerBil = "NAV 10-07.40"
private val erHovedSkjemaBil = true
private val tittelBil = "Søknad om stønad til anskaffelse av motorkjøretøy"
private val uuidBil = "e7179251-635e-493a-948c-749a39eedacc"
private val filNavnBil = "skjemanummerBil + filformat burde den vært"
private val filStorrelseBil = "10000"
val variantformatBilHovedskjema = "ARKIV"
//Vedlegg
//Kvittering
private val uuidBilKvittering = "e7179251-635e-493a-948c-749a39kviter"
private val filnavnKvitteering = "kvittering.pdf"
private val tittelKvitteering = "kvittering"
private val skjemanummerKvittering = "L7"
// forerkort
private val uuidBILvedlegg = "e7179251-635e-493a-948c-749a39vedleg"
private val filnavnForerkort = "forerkort.pdf"
private val tittelForerkort = "Kopi av førerkort"
private val skjemanummerForerkort = "Z4"
// felles for vedlegg
private val filstorrelseVedlegg = "101010"
private val erIkkeHovedskjemaBil = false
private val variantformatOrginal = "ORGINAL"
private val mimeTypeBil = "er det bruk for denne? bør vel være dokumenttype" // pdf, xml, json, pdfa


fun opprettBilInnsendingMedBareSoknadOgKvittering(): SoknadInnsendtDto {
	val soknad : InnsendtVariantDto = opprettHoveddokumentVariant()
	val kvittering : InnsendtVariantDto = opprettKvitteringVariant()
	val soknadsDokument: InnsendtDokumentDto = innsendtHovedskjemaDokument(soknad)
	val kvitteringDokument: InnsendtDokumentDto = innsendtHovedskjemaDokument(kvittering)

	val listeAvDokumenter = mutableListOf(kvitteringDokument, soknadsDokument)
	return SoknadInnsendtDto(innsendingsidIdForBilForsendelse, false, personIDBil, temaBil, LocalDateTime.now(), listeAvDokumenter)
}

fun opprettForerkortVedleggMedVariantOgDokument(): InnsendtDokumentDto {
	val forerkortVariant = opprettForerkortSomVedleggVariant()
	return InnsendtDokumentDto(skjemanummerBil, false, tittelForerkort, mutableListOf(forerkortVariant))
}

// Oppretter varianter
fun opprettDokumentVarianter(): Triple<InnsendtVariantDto, InnsendtVariantDto, InnsendtVariantDto> {
	val variantHovedskjemaBIL = opprettHoveddokumentVariant()
	val variantKvitteringBil = opprettKvitteringVariant()
	val variantForerkort = opprettForerkortSomVedleggVariant()
	return Triple(variantHovedskjemaBIL, variantKvitteringBil, variantForerkort)
}

fun opprettForerkortSomVedleggVariant() = InnsendtVariantDto(uuidBILvedlegg, mimeTypeBil, filnavnForerkort, filstorrelseVedlegg, variantformatOrginal, "PDF")

fun opprettKvitteringVariant()= InnsendtVariantDto(uuidBilKvittering, mimeTypeBil, filnavnKvitteering, filstorrelseVedlegg, variantformatOrginal, "PDF")

fun opprettHoveddokumentVariant() = InnsendtVariantDto(uuidBil, mimeTypeBil, filNavnBil, filStorrelseBil, variantformatBilHovedskjema, "PDF/A")

fun innsendtHovedskjemaDokument(variantHovedskjemaBil: InnsendtVariantDto) = InnsendtDokumentDto(skjemanummerBil, erHovedSkjemaBil, tittelBil, listOf(variantHovedskjemaBil))
