package no.nav.soknad.arkivering.soknadsmottaker.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.prometheus.client.CollectorRegistry
import no.nav.soknad.arkivering.soknadsmottaker.config.AppConfiguration
import no.nav.soknad.arkivering.soknadsmottaker.supervision.InnsendtMetrics
import no.nav.soknad.arkivering.soknadsmottaker.utils.createSoknad
import org.junit.jupiter.api.Test
import java.util.*

class ArchiverServiceTests {

	private val topic = "privat-soknadInnsendt-v1-teamsoknad"
	private val metricsTopic = "privat-soknadInnsendt-metrics-v1-teamsoknad"

	private val config = AppConfiguration()
	private val kafkaSender = mockk<KafkaSender>()

	@Test
	fun `Calls Kafka sender`() {
		every { kafkaSender.publish(any(), any(), any()) } returns Unit
		every { kafkaSender.publishMetric(any(), any(), any()) } returns Unit

		val metrics = InnsendtMetrics(CollectorRegistry.defaultRegistry)
		val archiverService = ArchiverService(kafkaSender, config, metrics)

		archiverService.archive(UUID.randomUUID().toString(), createSoknad())

		verify { kafkaSender.publish(topic, any(), any()) }
		verify { kafkaSender.publishMetric(metricsTopic, any(), any()) }
	}
}
