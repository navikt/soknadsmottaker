package no.nav.soknad.arkivering.soknadsmottaker.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import no.nav.soknad.arkivering.soknadsmottaker.config.AppConfiguration
import no.nav.soknad.arkivering.soknadsmottaker.dto.opprettBilInnsendingMedBareSoknadOgKvittering
import no.nav.soknad.arkivering.soknadsmottaker.supervise.InnsendtMetrics
import no.nav.soknad.arkivering.soknadsmottaker.supervise.MicroMetrics
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ArchiverServiceTests {

	private val config = AppConfiguration()

/*
	@Test
	fun `Kaller Kafka sender`() {
		val kafkaSender = mock<KafkaSender> { }

		val metrics = InnsendtMetrics()
		val archiverService = ArchiverService(kafkaSender, config, metrics)

		val metricsBefore = metrics.mottattSoknadGet("BIL")
		archiverService.archive(opprettBilInnsendingMedBareSoknadOgKvittering())

		verify(kafkaSender, times(1)).publish(any(), any(), any())
		if (metricsBefore != null) {
			assertEquals(metricsBefore + 1.0 , metrics.mottattSoknadGet("BIL")!!)
			assertEquals(metricsBefore + 1.0 , MicroMetrics.mottattSoknadGet("BIL"))
		} else {
			assertTrue(metrics.mottattSoknadGet("BIL")!! >= 1.0)
			assertTrue(MicroMetrics.mottattSoknadGet("BIL") >= 1.0)
		}
	}

*/

	@Test
	fun `Sjekker innlesning av miljovariable`() {
		println(config)
		assertEquals(config.kafkaConfig.username, "kafkaproducer")
		assertEquals(config.kafkaConfig.topic, "privat-soknadInnsendt-v1-default")
		assertEquals(config.restConfig.user, "avsender")
		assertEquals(config.restConfig.password, "password")
	}
}
