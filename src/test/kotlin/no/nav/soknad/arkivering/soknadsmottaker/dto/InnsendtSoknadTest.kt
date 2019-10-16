package no.nav.soknad.arkivering.soknadsmottaker.dto

import no.nav.soknad.arkivering.dto.InnsendtDokumentDto
import no.nav.soknad.arkivering.dto.InnsendtVariantDto
import no.nav.soknad.arkivering.dto.SoknadInnsendtDto
import org.joda.time.DateTime
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime

@SpringBootTest
class InnsendtSoknadTest() {

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
	private val filtypeForBILSoknad = "PDF/A"
	//Vedlegg
	//Kvittering
	private val uuidBilKvittering = "e7179251-635e-493a-948c-749a39kviter"
	private val filnavnKvitteering = "kvittering.pdf"
	private val tittelKvitteering = "kvittering"
	private val skjemanummerKvittering = "L7"
	private val filtypeforKvittering = "PDF/A"
	// forerkort
	private val uuidBILvedlegg = "e7179251-635e-493a-948c-749a39vedleg"
	private val filnavnForerkort = "forerkort.pdf"
	private val tittelForerkort = "Kopi av førerkort"
	private val skjemanummerForerkort = "Z4"
	private val filtypeforForerkort = "PDF"
	// felles for vedlegg
	private val filstorrelseVedlegg = "101010"
	private val erIkkeHovedskjemaBil = false
	private val variantformatOrginal = "ORGINAL"
	private val mimeTypeBil = "er det bruk for denne? bør vel være dokumenttype" // pdf, xml, json, pdfa


	private fun `mottak av BIL forstegangs innsending`(){

		val variantHovedskjemaBil = InnsendtVariantDto(uuidBil, mimeTypeBil, filNavnBil, filStorrelseBil, variantformatBilHovedskjema, filtypeForBILSoknad)
		val variantKvitteringBil = InnsendtVariantDto(uuidBilKvittering, mimeTypeBil, filnavnKvitteering, filstorrelseVedlegg, variantformatOrginal, filtypeforKvittering)
		val variantForerkort = InnsendtVariantDto(uuidBILvedlegg, mimeTypeBil, filnavnForerkort, filstorrelseVedlegg, variantformatOrginal, filtypeforForerkort)

		val bilHovedskjemaDokument = InnsendtDokumentDto (skjemanummerBil, erHovedSkjemaBil, tittelBil, varianter = listOf(variantHovedskjemaBil))
		val bilKvitteringDokument = InnsendtDokumentDto( skjemanummerKvittering, erIkkeHovedskjemaBil, tittelKvitteering, varianter = listOf(variantKvitteringBil))
		val bilForerkortDokument = InnsendtDokumentDto (skjemanummerForerkort, erIkkeHovedskjemaBil, tittelForerkort, varianter = listOf(variantForerkort))

		val innsendteDokumenterBil = mutableListOf<InnsendtDokumentDto>(bilHovedskjemaDokument, bilKvitteringDokument, bilForerkortDokument)

		val innsendtBilSoknad = SoknadInnsendtDto (innsendingsidIdForBilForsendelse, erEttersendelseBil, personIDBil, temaBil,
			this.innsendtDatoBil, innsendteDokumenter = innsendteDokumenterBil)

	}


}
