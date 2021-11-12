package no.nav.soknad.arkivering.soknadsmottaker.rest

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.prometheus.client.CollectorRegistry
import no.nav.soknad.arkivering.avroschemas.InnsendingMetrics
import no.nav.soknad.arkivering.avroschemas.Soknadarkivschema
import no.nav.soknad.arkivering.soknadsmottaker.config.AppConfiguration
import no.nav.soknad.arkivering.soknadsmottaker.service.ArchiverService
import no.nav.soknad.arkivering.soknadsmottaker.service.KafkaSender
import no.nav.soknad.arkivering.soknadsmottaker.service.ReSender
import no.nav.soknad.arkivering.soknadsmottaker.supervision.InnsendtMetrics
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class ReSendTests {

	private val kafkaSenderMock = mockk<KafkaSender>()

	private val metrics = InnsendtMetrics(CollectorRegistry(true))

	@Test
	fun `When ReSender starts applications is resent`() {
		val electorPath = getResource("/elector_path.json").toString()
		System.setProperty("ELECTOR_PATH", electorPath)
		System.setProperty("RESENDING_LIST", getBytesFromFile("/resend-applications.json"))

		val errorsBefore = metrics.mottattErrorGet("TSO")
		val sentInBefore = metrics.mottattSoknadGet("TSO")

		val capturedSoknadarkivschema = slot<Soknadarkivschema>()
		val capturedMetric = slot<InnsendingMetrics>()

		every { kafkaSenderMock.publish(any(), capture(capturedSoknadarkivschema), any()) } returns Unit
		every { kafkaSenderMock.publishMetric(any(), capture(capturedMetric), any()) } returns Unit

		startResender(metrics, AppConfiguration())

		// Vent til resender har kjørt
		TimeUnit.SECONDS.sleep(2)

		assertTrue(capturedSoknadarkivschema.isCaptured)
		assertEquals("TSO", capturedSoknadarkivschema.captured.arkivtema, "Should have correct tema")
		assertEquals(errorsBefore!! + 0.0, metrics.mottattErrorGet("TSO"), "Should not cause errors")
		assertEquals(sentInBefore!! + 1.0, metrics.mottattSoknadGet("TSO"), "Should increase counter by 1")

		assertTrue(capturedMetric.isCaptured)
		assertEquals("soknadsmottaker", capturedMetric.captured.application, "Metrics should have correct application name")
		assertEquals("publish to kafka", capturedMetric.captured.action, "Metrics should have correct action")
		assertTrue(capturedMetric.captured.startTime <= System.currentTimeMillis(), "Metrics should have correct startTime")
		assertTrue(capturedMetric.captured.duration <= capturedMetric.captured.startTime, "Metrics should have a duration")

		metrics.unregister()
	}

	@Test
	fun `When not Leader, ReSender sends no applications`() {
		val electorPath = getResource("/not_leader.json").toString()
		System.setProperty("ELECTOR_PATH", electorPath)
		System.setProperty("RESENDING_LIST", getBytesFromFile("/resend-applications.json"))

		val capturedSoknadarkivschema = slot<Soknadarkivschema>()
		val capturedMetric = slot<InnsendingMetrics>()

		every { kafkaSenderMock.publish(any(), capture(capturedSoknadarkivschema), any()) } returns Unit
		every { kafkaSenderMock.publishMetric(any(), capture(capturedMetric), any()) } returns Unit

		startResender(metrics, AppConfiguration())

		// Vent til resender har kjørt
		TimeUnit.SECONDS.sleep(2)

		assertFalse(capturedSoknadarkivschema.isCaptured)
		assertFalse(capturedMetric.isCaptured)

		metrics.unregister()
	}


	private fun startResender(metrics: InnsendtMetrics, appConfiguration: AppConfiguration): ReSender {
		val orderService = ArchiverService(kafkaSenderMock, metrics)
		return ReSender(orderService, appConfiguration)
	}


	private fun getResource(fileName: String) = this::class.java.getResource(fileName)!!

	private fun getBytesFromFile(@Suppress("SameParameterValue") path: String): String {
		val resourceAsStream = this::class.java.getResourceAsStream(path)
		val outputStream = ByteArrayOutputStream()
		resourceAsStream.use { input ->
			outputStream.use { output ->
				input!!.copyTo(output)
			}
		}
		return outputStream.toString()
	}
}
