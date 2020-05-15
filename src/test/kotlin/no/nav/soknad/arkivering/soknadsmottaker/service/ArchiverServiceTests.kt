package no.nav.soknad.arkivering.soknadsmottaker.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import no.nav.soknad.arkivering.soknadsmottaker.config.AppConfiguration
import no.nav.soknad.arkivering.soknadsmottaker.dto.opprettBilInnsendingMedBareSoknadOgKvittering
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ArchiverServiceTests {

	private val kafkaSender = mock<KafkaSender> { }
	private val config = AppConfiguration()

	private val archiverService = ArchiverService(kafkaSender,config)

	@Test
	fun `Kaller Kafka sender`() {
		archiverService.archive(opprettBilInnsendingMedBareSoknadOgKvittering())

		verify(kafkaSender, times(1)).publish(any(), any(), any())
	}


	@Test
	fun `Sjekker innlesning av miljovariable`() {
		println(config)
		assertEquals(config.kafkaConfig.username, "kafkaproducer")
		assertEquals(config.kafkaConfig.topic, "privat-soknadInnsendt-v1-default")
		assertEquals(config.restConfig.user, "avsender")
		assertEquals(config.restConfig.password, "password")
	}
}
