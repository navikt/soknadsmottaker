package no.nav.soknad.arkivering.soknadsmottaker.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import no.nav.soknad.arkivering.dto.SoknadMottattDto
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ArchiverServiceTests {

	private val kafkaSender = mock<KafkaSender> { }
	private val archiverService = ArchiverService(kafkaSender)
	//Transformert data fra innsendt til mottatt
	//SoknadmotattDto
	private val eksternReferanseIDBil = "123445666"
	private val personIdBIL = "12345678910"
	private val temaBIL = "BIL"
	//MottattDokumentDto
	private val skjemanummerBIL = "NAV 10-07.40"
	private val erhovedskjemaBIL = true
	private val tittelBIL = "Søknad om stønad til anskaffelse av motorkjøretøy"
	// motattVariantDto
	private val uuidBIL = "e7179251-635e-493a-948c-749a39eedacc"
	private val filNavnBil = skjemanummerBIL


	@Test
	fun `Kaller Kafka sender`() {
		archiverService.archive(opprettMeldingPaKafka())

		verify(kafkaSender, times(1)).publish(any(), any(), any())
	}

	private fun opprettMeldingPaKafka(): SoknadMottattDto {
		return SoknadMottattDto(eksternReferanseIDBil, personIdBIL, "FNR", "BIL", LocalDateTime.now(), listOf())
	}
}
