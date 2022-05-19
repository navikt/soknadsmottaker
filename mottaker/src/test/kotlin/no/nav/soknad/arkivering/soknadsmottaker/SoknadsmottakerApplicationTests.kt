package no.nav.soknad.arkivering.soknadsmottaker

import no.nav.soknad.arkivering.soknadsmottaker.config.KafkaConfig
import no.nav.soknad.arkivering.soknadsmottaker.config.RestConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SoknadsmottakerApplicationTests {

	@Autowired
	private lateinit var restConfig: RestConfig
	@Autowired
	private lateinit var kafkaConfig: KafkaConfig

	@Test
	fun `Spring context loads`() {
	}

	@Test
	fun `Reads environment variables correctly`() {
		assertEquals("privat-soknadinnsending-v1-dev", kafkaConfig.mainTopic)
		assertEquals("privat-soknadinnsending-metrics-v1-dev", kafkaConfig.metricsTopic)
		assertEquals("innsending", restConfig.username)
		assertEquals("password", restConfig.password)
	}
}
