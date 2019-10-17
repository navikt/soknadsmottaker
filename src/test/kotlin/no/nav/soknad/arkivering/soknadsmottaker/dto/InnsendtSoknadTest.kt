package no.nav.soknad.arkivering.soknadsmottaker.dto

import no.nav.soknad.arkivering.dto.InnsendtDokumentDto
import no.nav.soknad.arkivering.dto.InnsendtVariantDto
import no.nav.soknad.arkivering.dto.SoknadInnsendtDto
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

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
	private val filtypeforKvittering = "PDF"
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

	// varianter
	private	val variantHovedskjemaBil = InnsendtVariantDto(uuidBil, mimeTypeBil, filNavnBil, filStorrelseBil, variantformatBilHovedskjema, filtypeForBILSoknad)
	private	val variantKvitteringBil = InnsendtVariantDto(uuidBilKvittering, mimeTypeBil, filnavnKvitteering, filstorrelseVedlegg, variantformatOrginal, filtypeforKvittering)
	private	val variantForerkort = InnsendtVariantDto(uuidBILvedlegg, mimeTypeBil, filnavnForerkort, filstorrelseVedlegg, variantformatOrginal, filtypeforForerkort)
	//Innsendte dokumenter
	private	val bilHovedskjemaDokument = InnsendtDokumentDto (skjemanummerBil, erHovedSkjemaBil, tittelBil, varianter = listOf(variantHovedskjemaBil))
	private	val bilKvitteringDokument = InnsendtDokumentDto( skjemanummerKvittering, erIkkeHovedskjemaBil, tittelKvitteering, varianter = listOf(variantKvitteringBil))
	private	val bilForerkortDokument = InnsendtDokumentDto (skjemanummerForerkort, erIkkeHovedskjemaBil, tittelForerkort, varianter = listOf(variantForerkort))
	private val innsendteDokumenterBil = mutableListOf<InnsendtDokumentDto>(bilHovedskjemaDokument, bilKvitteringDokument, bilForerkortDokument)

	private val innsendtBilSoknad = SoknadInnsendtDto (innsendingsidIdForBilForsendelse, erEttersendelseBil, personIDBil, temaBil,
			this.innsendtDatoBil, innsendteDokumenter = innsendteDokumenterBil)



	@Test
	fun `opprett Soknad med bare Hovedskjema`() {
		Assertions.assertTrue(bilHovedskjemaDokument.varianter::isNotEmpty)
	}

	@Test
	fun `innsendt soknad om bilstonad har 3 dokumenter`(){
		Assertions.assertEquals(3, innsendteDokumenterBil.count())
	}

}
