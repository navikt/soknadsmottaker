package no.nav.soknad.arkivering.soknadsmottaker.rest

import no.nav.soknad.arkivering.soknadsmottaker.config.AppConfiguration
import no.nav.soknad.arkivering.soknadsmottaker.dto.opprettBilInnsendingMedBareSoknadOgKvittering
import no.nav.soknad.arkivering.soknadsmottaker.service.ArchiverService
import no.nav.soknad.arkivering.soknadsmottaker.service.KafkaSender
import no.nav.soknad.soknadarkivering.avroschemas.Soknadarkivschema
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.kafka.core.KafkaTemplate

class ReceiverTests() {

	private val kafkaMock: KafkaTemplate<String, Soknadarkivschema> = mock()
	private val receiver = mockReceiver()

	@Test
	fun `Ved mottatt REST call, legg melding paa Kafka`() {
		val melding = opprettBilInnsendingMedBareSoknadOgKvittering()

		receiver.receiveMessage(melding)

		verify(kafkaMock, times(1))
			.send(Mockito.eq("privat-soknadInnsendt-v1-default"), Mockito.eq("personId"), Mockito.any())
	}

	private fun mockReceiver(): Receiver {
		val kafkaSender = KafkaSender(kafkaMock)
		val config: AppConfiguration = AppConfiguration()
		val orderService = ArchiverService(kafkaSender, config)
		return Receiver(orderService)
	}

	private inline fun <reified T : Any> mock() = Mockito.mock(T::class.java)!!
}
