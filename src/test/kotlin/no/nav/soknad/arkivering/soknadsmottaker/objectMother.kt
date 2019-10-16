package no.nav.soknad.arkivering.soknadsmottaker

import no.nav.soknad.arkivering.dto.InnsendtDokumentDto
import no.nav.soknad.arkivering.dto.InnsendtVariantDto
import no.nav.soknad.arkivering.dto.SoknadInnsendtDto
import org.joda.time.DateTime

internal class objectMother(){
	//forsendelse informasjon
	private val innsendingsidIdForBilForsendelse = "IS123456"
	private var erEttersendelseBil = false
	private val personIDBil = "12345678910"
	private val temaBil = "BIL"
	private val innsendtDatoBil = DateTime.now()

	// hovedskjema
	private val skjemanummerBil = "NAV 10-07.40"
	private val erHovedSkjemaBil = true
	private val tittelBil = "Søknad om stønad til anskaffelse av motorkjøretøy"
	private val uuidBil = "e7179251-635e-493a-948c-749a39eedacc"
	private val filNavnBil = skjemanummerBil
	private val filStorrelseBil = "10000"
	private val variantformatBilHovedskjema = "ARKIV"
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

	internal fun `opprettBilForsendelse`(){

		/*val (variantHovedskjemaBIL, variantKvitteringBil, variantForerkort) = opprettDokumentVarianterForBIL()

		val (bilHovedskjemaDokument, bilKvitteringDokument, bilForerkortDokument) = opprettDokumenterForBIL(variantHovedskjemaBIL, variantKvitteringBil, variantForerkort)

		val innsendteDokumenterBil = mutableListOf<InnsendtDokumentDto>(bilHovedskjemaDokument, bilKvitteringDokument, bilForerkortDokument)

		val innsendtBilSoknad = SoknadInnsendtDto (innsendingsidIdForBilForsendelse, erEttersendelseBil, personIDBil, temaBil,
			this.innsendtDatoBil, innsendteDokumenter = innsendteDokumenterBil)

	}

	fun opprettDokumentVarianterForBIL(): Triple<InnsendtVariantDto, InnsendtVariantDto, InnsendtVariantDto> {
		val variantHovedskjemaBIL = opprettHoveddokumentVariant()
		val variantKvitteringBil = opprettKvitteringVariant()
		val variantForerkort = opprettForerkortSomVedleggVariant()
		return Triple(variantHovedskjemaBIL, variantKvitteringBil, variantForerkort)
	}

	fun opprettForerkortSomVedleggVariant() =
		InnsendtVariantDto(uuidBILvedlegg, mimeTypeBil, filnavnForerkort, filstorrelseVedlegg, variantformatOrginal)

	fun opprettKvitteringVariant(): InnsendtVariantDto {
		val variantKvitteringBil = InnsendtVariantDto(uuidBilKvittering, mimeTypeBil, filnavnKvitteering, filstorrelseVedlegg, variantformatOrginal)
		return variantKvitteringBil
	}

	fun opprettHoveddokumentVariant() = InnsendtVariantDto(uuidBil, mimeTypeBil, filNavnBil, filStorrelseBil, variantformatBilHovedskjema)

	private fun opprettDokumenterForBIL(variantHovedskjemaBil: InnsendtVariantDto, variantKvitteringBil: InnsendtVariantDto, variantForerkort: InnsendtVariantDto): Triple<InnsendtDokumentDto, InnsendtDokumentDto, InnsendtDokumentDto> {
		val bilHovedskjemaDokument = InnsendtDokumentDto(skjemanummerBil, erHovedSkjemaBil, tittelBil, varianter = listOf(variantHovedskjemaBil))
		val bilKvitteringDokument = InnsendtDokumentDto(skjemanummerKvittering, erIkkeHovedskjemaBil, tittelKvitteering, varianter = listOf(variantKvitteringBil))
		val bilForerkortDokument = InnsendtDokumentDto(skjemanummerForerkort, erIkkeHovedskjemaBil, tittelForerkort, varianter = listOf(variantForerkort))
		return Triple(bilHovedskjemaDokument, bilKvitteringDokument, bilForerkortDokument)*/
	}
}
