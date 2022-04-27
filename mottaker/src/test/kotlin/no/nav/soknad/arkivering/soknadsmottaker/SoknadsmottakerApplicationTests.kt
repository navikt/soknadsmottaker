package no.nav.soknad.arkivering.soknadsmottaker

import no.nav.soknad.arkivering.soknadsmottaker.config.AppConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SoknadsmottakerApplicationTests {

	@Autowired
	private lateinit var config: AppConfiguration

	@Test
	fun `Spring context loads`() {
	}

	@Test
	fun `Reads environment variables correctly`() {
		assertEquals("privat-soknadInnsendt-v1-teamsoknad", config.kafkaConfig.mainTopic)
		assertEquals("privat-soknadInnsendt-metrics-v1-teamsoknad", config.kafkaConfig.metricsTopic)
		assertEquals("innsending", config.restConfig.username)
		assertEquals("password", config.restConfig.password)
	}
}
