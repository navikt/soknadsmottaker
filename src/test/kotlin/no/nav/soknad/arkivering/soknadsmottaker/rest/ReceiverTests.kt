package no.nav.soknad.arkivering.soknadsmottaker.rest

import no.nav.soknad.arkivering.dto.SoknadInnsendtDto
import no.nav.soknad.arkivering.dto.SoknadMottattDto
import no.nav.soknad.arkivering.soknadsmottaker.service.ArchiverService
import no.nav.soknad.arkivering.soknadsmottaker.service.KafkaSender
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.kafka.core.KafkaTemplate
import java.time.LocalDateTime

class ReceiverTests {

	private val kafkaMock: KafkaTemplate<String, SoknadMottattDto> = mock()
	private val receiver = mockReceiver()
	// TODO generaliser med objectMother
	private val innsendingsidIdForBilForsendelse = "IS123456"
	private val personIDBil = "12345678910"
	private val temaBil = "BIL"
	private val skjemanummerBil = "NAV 10-07.40"
	private val erHovedSkjemaBil = true
	private val tittelBil = "Søknad om stønad til anskaffelse av motorkjøretøy"
	private val uuidBil = "e7179251-635e-493a-948c-749a39eedacc"
	private val filNavnBil = skjemanummerBil
	private val filStorrelseBil = "10000"
	private val variantformatBilHovedskjema = "ARKIV"
	private val mimeTypeBil = "er det bruk for denne? bør vel være dokumenttype" // pdf, xml, json, pdfa

	@Test
	fun `Ved mottatt REST call, legg melding paa Kafka`() {
		val melding = opprettMelding()

		receiver.receiveMessage(melding)

		verify(kafkaMock, times(1))
			.send(Mockito.eq("privat-soknadInnsendt-sendsoknad-v1-q0"), Mockito.eq("personId"), Mockito.any())
	}

	private fun opprettMelding(): SoknadInnsendtDto {
		return SoknadInnsendtDto(innsendingsidIdForBilForsendelse, false , personIDBil, temaBil, LocalDateTime.now(), listOf())
	}

	private fun mockReceiver(): Receiver {
		val kafkaSender = KafkaSender(kafkaMock)
		val orderService = ArchiverService(kafkaSender)
		return Receiver(orderService)
	}

	private inline fun <reified T: Any> mock() = Mockito.mock(T::class.java)!!
}
