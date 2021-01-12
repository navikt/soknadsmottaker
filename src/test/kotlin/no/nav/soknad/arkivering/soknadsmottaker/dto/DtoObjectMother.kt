package no.nav.soknad.arkivering.soknadsmottaker.dto

import java.time.LocalDateTime
import java.util.*

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

// felles for vedlegg
private const val filstorrelseVedlegg = "101010"
private const val erIkkeHovedskjemaBil = false
private const val mimeTypeBil = "er det bruk for denne? bør vel være dokumenttype" // pdf, xml, json, pdfa


fun opprettBilInnsendingMedBareSoknadOgKvittering(): SoknadInnsendtDto {
	val soknad: InnsendtVariantDto = opprettHoveddokumentVariant()
	val kvittering: InnsendtVariantDto = opprettKvitteringVariant()
	val soknadsDokument: InnsendtDokumentDto = innsendtHovedskjemaDokument(soknad)
	val kvitteringDokument: InnsendtDokumentDto = innsendtKvitteringDokument(kvittering)

	val listeAvDokumenter = mutableListOf(kvitteringDokument, soknadsDokument)
	return SoknadInnsendtDto(
		innsendingsidIdForBilForsendelse,
		erEttersendelseBil,
		personIDBil,
		temaBil,
		innsendtDatoBil,
		listeAvDokumenter
	)
}

fun opprettSoknadUtenFilnavnSatt(): SoknadInnsendtDto {
	val soknad: InnsendtVariantDto = opprettHoveddokumentVariant()
	val kvittering: InnsendtVariantDto = opprettKvitteringVariant()
	val annet: InnsendtVariantDto = opprettAnnetDokumentVariant()
	val soknadsDokument: InnsendtDokumentDto = innsendtHovedskjemaDokument(soknad)
	val kvitteringDokument: InnsendtDokumentDto = innsendtKvitteringDokument(kvittering)
	val annetDokument: InnsendtDokumentDto = innsendtAnnetDokument(annet)

	val listeAvDokumenter = mutableListOf(kvitteringDokument, soknadsDokument, annetDokument)
	return SoknadInnsendtDto(innsendingsidIdForBilForsendelse, erEttersendelseBil, personIDBil, temaBil, innsendtDatoBil, listeAvDokumenter)

}

private fun opprettKvitteringVariant() =
	InnsendtVariantDto(
		uuidBilKvittering,
		mimeTypeBil,
		filnavnKvitteering,
		filstorrelseVedlegg,
		variantformatBilKvittering,
		filtypeBilKvittering
	)

fun opprettHoveddokumentVariant() =
	InnsendtVariantDto(
		uuidBil,
		mimeTypeBil,
		filNavnBil,
		filStorrelseBil,
		variantformatBilHovedskjema,
		filtypeBilHoveskjema
	)

private fun opprettAnnetDokumentVariant() =
	InnsendtVariantDto(UUID.randomUUID().toString(), mimeTypeBil, null, null, variantformatBilKvittering, filtypeBilKvittering)

fun innsendtHovedskjemaDokument(variantHovedskjemaBil: InnsendtVariantDto) =
	InnsendtDokumentDto(skjemanummerBil, erHovedSkjemaBil, tittelBil, listOf(variantHovedskjemaBil))

private fun innsendtKvitteringDokument(variantKvitteringBil: InnsendtVariantDto) =
	InnsendtDokumentDto(skjemanummerKvittering, erIkkeHovedskjemaBil, tittelKvitteering, listOf(variantKvitteringBil))

private fun innsendtAnnetDokument(variantAnnet: InnsendtVariantDto) =
	InnsendtDokumentDto(skjemanummerKvittering, erIkkeHovedskjemaBil, tittelKvitteering, listOf(variantAnnet))
