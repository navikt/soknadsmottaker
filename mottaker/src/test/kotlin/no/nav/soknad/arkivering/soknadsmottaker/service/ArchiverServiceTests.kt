package no.nav.soknad.arkivering.soknadsmottaker.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.soknad.arkivering.soknadsmottaker.supervision.InnsendtMetrics
import no.nav.soknad.arkivering.soknadsmottaker.utils.createSoknad
import org.junit.jupiter.api.Test
import java.util.*

class ArchiverServiceTests {
	private val kafkaSender = mockk<KafkaSender>()

	private val metrics = mockk<InnsendtMetrics>(relaxed = true)

	@Test
	fun `Calls Kafka sender`() {
		every { kafkaSender.publishSoknadarkivschema(any(), any()) } returns Unit
		every { kafkaSender.publishMetric(any(), any()) } returns Unit

		val archiverService = ArchiverService(kafkaSender, metrics)

		archiverService.archive(UUID.randomUUID().toString(), createSoknad())

		verify { kafkaSender.publishSoknadarkivschema(any(), any()) }
		verify { kafkaSender.publishMetric(any(), any()) }
	}
}
