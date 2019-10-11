package no.nav.soknad.arkivering.soknadsmottaker.rest

import no.nav.soknad.arkivering.dto.InnsendtDokumentDto
import no.nav.soknad.arkivering.dto.SoknadInnsendtDto
import no.nav.soknad.arkivering.dto.SoknadMottattDto
import no.nav.soknad.arkivering.soknadsmottaker.service.ArchiverService
import no.nav.soknad.arkivering.soknadsmottaker.service.KafkaSender
import org.joda.time.DateTime
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.kafka.core.KafkaTemplate
import java.util.*

class ReceiverTests {

	private val kafkaMock: KafkaTemplate<String, SoknadMottattDto> = mock()
	private val receiver = mockReceiver()

	@Test
	fun `When receiving REST call, message is put on Kafka`() {
		val message = createMessage()

		receiver.receiveMessage(message)

		verify(kafkaMock, times(1))
			.send(Mockito.eq("archival"), Mockito.eq("personId"), Mockito.any())
	}

	private fun createMessage(): SoknadInnsendtDto {
		val innsendtDokumentDto =  InnsendtDokumentDto("123456789","NAV 11-12.12", false
			, true,"Eksempel","application/pdf", "NAV 11-12.12", 100)

		return SoknadInnsendtDto("100","99","01018012345","TSO", DateTime.now(), Arrays.asList(innsendtDokumentDto))
	}

	private fun mockReceiver(): Receiver {
		val kafkaSender = KafkaSender(kafkaMock)
		val orderService = ArchiverService(kafkaSender)
		return Receiver(orderService)
	}

	private inline fun <reified T: Any> mock() = Mockito.mock(T::class.java)!!
}
