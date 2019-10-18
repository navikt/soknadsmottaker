package no.nav.soknad.arkivering.dto

import org.joda.time.DateTime
import org.junit.jupiter.api.Test


//forsendelse informasjon
	private val innsendingsidIdForBilForsendelse = "IS123456"
	private var erEttersendelseBil = false
	private val personIDBil = "12345678910"
	private val temaBil = "BIL"
	private val innsendtDatoBil = DateTime.now()
	// hovedskjema
	val skjemanummerBil = "NAV 10-07.40"
	private val erHovedSkjemaBil = true
	val tittelBil = "Søknad om stønad til anskaffelse av motorkjøretøy"
	val uuidBil = "e7179251-635e-493a-948c-749a39eedacc"
	val filNavnBil = "skjemanummerBil + filformat burde den vært"
	private val filStorrelseBil = "10000"
	val variantformatBilHovedskjema = "ARKIV"
	//Vedlegg
	//Kvittering
	private val uuidBilKvittering = "e7179251-635e-493a-948c-749a39kviter"
	val filnavnKvitteering = "kvittering.pdf"
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
		val kvitteringDokument: InnsendtDokumentDto = innsendtKvitteringDokument(kvittering)

		val listeAvDokumenter = mutableListOf<InnsendtDokumentDto>(kvitteringDokument,soknadsDokument)
		return (SoknadInnsendtDto(innsendingsidIdForBilForsendelse, false, personIDBil,temaBil, DateTime.now(), innsendteDokumenter = listeAvDokumenter))
	  }

	fun opprettForerkortVedleggMedVariantOgDokument(): InnsendtDokumentDto {
		val forerkortVariant = opprettForerkortSomVedleggVariant()
		return (InnsendtDokumentDto(skjemanummerBil,false, tittelForerkort, mutableListOf(forerkortVariant)))
		}

	fun opprettForerkortSomVedleggVariant(): InnsendtVariantDto =
		InnsendtVariantDto(uuidBILvedlegg, mimeTypeBil, filnavnForerkort,filstorrelseVedlegg,variantformatOrginal,"PDF" )

	fun opprettKvitteringVariant(): InnsendtVariantDto =	InnsendtVariantDto(uuidBilKvittering, mimeTypeBil, filnavnKvitteering, filstorrelseVedlegg, variantformatOrginal,"PDF")

	fun opprettHoveddokumentVariant(): InnsendtVariantDto = InnsendtVariantDto(uuidBil, mimeTypeBil, filNavnBil, filStorrelseBil, variantformatBilHovedskjema, "PDF/A")

	fun innsendtHovedskjemaDokument(variantHovedskjemaBil: InnsendtVariantDto): InnsendtDokumentDto {
		val bilHovedskjemaDokument = InnsendtDokumentDto(skjemanummerBil, erHovedSkjemaBil, tittelBil, varianter = listOf(variantHovedskjemaBil))
		return bilHovedskjemaDokument
	  }
  fun innsendtKvitteringDokument(variantKvitteringBil: InnsendtVariantDto): InnsendtDokumentDto {
	  val bilHovedskjemaDokument = InnsendtDokumentDto(skjemanummerKvittering, erIkkeHovedskjemaBil, tittelKvitteering, varianter = listOf(variantKvitteringBil))
	  return bilHovedskjemaDokument
    }


