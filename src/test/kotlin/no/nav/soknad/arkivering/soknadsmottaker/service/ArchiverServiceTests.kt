package no.nav.soknad.arkivering.soknadsmottaker.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import io.prometheus.client.CollectorRegistry
import no.nav.soknad.arkivering.soknadsmottaker.config.AppConfiguration
import no.nav.soknad.arkivering.soknadsmottaker.dto.opprettBilInnsendingMedBareSoknadOgKvittering
import no.nav.soknad.arkivering.soknadsmottaker.supervise.InnsendtMetrics
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ArchiverServiceTests {

	private val config = AppConfiguration()
	private val kafkaSender = mock<KafkaSender> { }

	@Test
	fun `Kaller Kafka sender`() {

		val metrics = InnsendtMetrics(CollectorRegistry.defaultRegistry)
		val archiverService = ArchiverService(kafkaSender, config, metrics)

		archiverService.archive(opprettBilInnsendingMedBareSoknadOgKvittering())

		verify(kafkaSender, times(1)).publish(any(), any(), any())
	}


	@Test
	fun `Sjekker innlesning av miljovariable`() {
		assertEquals(config.kafkaConfig.username, "kafkaproducer")
		assertEquals(config.kafkaConfig.topic, "privat-soknadInnsendt-v1-teamsoknad")
		assertEquals(config.kafkaConfig.metricsTopic, "privat-soknadInnsendt-metrics-v1-teamsoknad")
		assertEquals(config.restConfig.user, "avsender")
		assertEquals(config.restConfig.password, "password")
	}
}
