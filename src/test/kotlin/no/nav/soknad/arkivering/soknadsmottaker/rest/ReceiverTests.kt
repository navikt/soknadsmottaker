package no.nav.soknad.arkivering.soknadsmottaker.rest

import no.nav.soknad.arkivering.dto.ArchivalData
import no.nav.soknad.arkivering.soknadsmottaker.service.ArchiverService
import no.nav.soknad.arkivering.soknadsmottaker.service.KafkaSender
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.kafka.core.KafkaTemplate

class ReceiverTests {

	private val kafkaMock: KafkaTemplate<String, ArchivalData> = mock()
	private val receiver = mockReceiver()

	@Test
	fun `When receiving REST call, message is put on Kafka`() {
		val message = "MSG"

		receiver.receiveMessage(message)

		verify(kafkaMock, times(1)).send(Mockito.eq("archival"), Mockito.eq("key"), Mockito.eq(ArchivalData("GSM", message)))
	}


	private fun mockReceiver(): Receiver {
		val kafkaSender = KafkaSender(kafkaMock)
		val orderService = ArchiverService(kafkaSender)
		return Receiver(orderService)
	}

	private inline fun <reified T: Any> mock() = Mockito.mock(T::class.java)!!
}
