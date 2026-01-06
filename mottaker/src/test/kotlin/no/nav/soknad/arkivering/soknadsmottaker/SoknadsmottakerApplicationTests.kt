package no.nav.soknad.arkivering.soknadsmottaker

import io.prometheus.metrics.model.registry.PrometheusRegistry
import no.nav.soknad.arkivering.soknadsmottaker.config.KafkaConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean

@ActiveProfiles("test")
@SpringBootTest(
	properties = ["spring.main.allow-bean-definition-overriding=true"],
	classes = [SoknadsmottakerApplication::class]
)
class SoknadsmottakerApplicationTests {

	@Autowired
	private lateinit var kafkaConfig: KafkaConfig

	@MockitoBean
	lateinit var prometheusRegistry: PrometheusRegistry

	@Test
	fun `Spring context loads`() {
	}

	@Test
	fun `Reads environment variables correctly`() {
		assertEquals("privat-soknadinnsending-v1-dev", kafkaConfig.mainTopic)
		assertEquals("privat-soknadinnsending-metrics-v1-dev", kafkaConfig.metricsTopic)
	}
}
