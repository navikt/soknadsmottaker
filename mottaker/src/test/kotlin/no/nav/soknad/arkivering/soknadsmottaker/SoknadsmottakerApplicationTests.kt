package no.nav.soknad.arkivering.soknadsmottaker

import io.prometheus.client.CollectorRegistry
import no.nav.soknad.arkivering.soknadsmottaker.config.KafkaConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest
class SoknadsmottakerApplicationTests {

	@Autowired
	private lateinit var kafkaConfig: KafkaConfig

	@MockBean
	lateinit var collectorRegistry: CollectorRegistry

	@Test
	fun `Spring context loads`() {
	}

	@Test
	fun `Reads environment variables correctly`() {
		assertEquals("privat-soknadinnsending-v1-dev", kafkaConfig.mainTopic)
		assertEquals("privat-soknadinnsending-metrics-v1-dev", kafkaConfig.metricsTopic)
	}
}
