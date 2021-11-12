package no.nav.soknad.arkivering.soknadsmottaker.rest

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.prometheus.client.CollectorRegistry
import no.nav.soknad.arkivering.avroschemas.InnsendingMetrics
import no.nav.soknad.arkivering.avroschemas.Soknadarkivschema
import no.nav.soknad.arkivering.soknadsmottaker.dto.opprettSoknadUtenFilnavnSatt
import no.nav.soknad.arkivering.soknadsmottaker.service.ArchiverService
import no.nav.soknad.arkivering.soknadsmottaker.service.KafkaSender
import no.nav.soknad.arkivering.soknadsmottaker.supervision.InnsendtMetrics
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ReceiverTests {

	private val kafkaSenderMock = mockk<KafkaSender>()

	private val metrics = InnsendtMetrics(CollectorRegistry(true))
	private val receiver = mockReceiver(metrics)

	@Test
	fun `When receiving REST call, message is put on Kafka`() {
		val errorsBefore = metrics.mottattErrorGet("BIL")
		val sentInBefore = metrics.mottattSoknadGet("BIL")
		val melding = opprettSoknadUtenFilnavnSatt()

		val capturedSoknadarkivschema = slot<Soknadarkivschema>()
		val capturedMetric = slot<InnsendingMetrics>()

		every { kafkaSenderMock.publish(any(), capture(capturedSoknadarkivschema), any()) } returns Unit
		every { kafkaSenderMock.publishMetric(any(), capture(capturedMetric), any()) } returns Unit

		receiver.receiveMessage(melding)

		assertTrue(capturedSoknadarkivschema.isCaptured)
		assertEquals("BIL", capturedSoknadarkivschema.captured.arkivtema, "Should have correct tema")
		assertEquals(errorsBefore!! + 0.0, metrics.mottattErrorGet("BIL"), "Should not cause errors")
		assertEquals(sentInBefore!! + 1.0, metrics.mottattSoknadGet("BIL"), "Should increase counter by 1")

		assertTrue(capturedMetric.isCaptured)
		assertEquals("soknadsmottaker", capturedMetric.captured.application, "Metrics should have correct application name")
		assertEquals("publish to kafka", capturedMetric.captured.action, "Metrics should have correct action")
		assertTrue(capturedMetric.captured.startTime <= System.currentTimeMillis(), "Metrics should have correct startTime")
		assertTrue(capturedMetric.captured.duration <= capturedMetric.captured.startTime, "Metrics should have a duration")

		metrics.unregister()
	}

	@Test
	fun `Exception is thrown if message is not put on Kafka`() {
		val melding = opprettSoknadUtenFilnavnSatt()

		val capturedSoknadarkivschema = slot<Soknadarkivschema>()
		val capturedMetric = slot<InnsendingMetrics>()

		every { kafkaSenderMock.publish(any(), capture(capturedSoknadarkivschema), any()) } throws Exception("Mocked Exception")
		every { kafkaSenderMock.publishMetric(any(), capture(capturedMetric), any()) } returns Unit

		assertThrows<Exception> {
			receiver.receiveMessage(melding)
		}

		assertTrue(capturedSoknadarkivschema.isCaptured)
		assertEquals("BIL", capturedSoknadarkivschema.captured.arkivtema, "Should have correct tema")
		metrics.unregister()
	}


	private fun mockReceiver(metrics: InnsendtMetrics): Receiver {
		val orderService = ArchiverService(kafkaSenderMock, metrics)
		return Receiver(orderService)
	}
}
