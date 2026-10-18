package no.nav.soknad.arkivering.soknadsmottaker.rest

import no.nav.soknad.arkivering.soknadsmottaker.SoknadsmottakerApplication
import no.nav.soknad.arkivering.soknadsmottaker.service.KafkaSender
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient

@ActiveProfiles("default")
@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	classes = [SoknadsmottakerApplication::class]
)
@AutoConfigureWebTestClient
class PrometheusEndpointTest {

	@Autowired
	lateinit var client: WebTestClient

	@MockitoBean
	lateinit var kafkaSender: KafkaSender

	@Test
	fun `Prometheus scrape without token returns metrics`() {
		client.get().uri("/internal/prometheus")
			.header("Accept", "text/plain; version=0.0.4")
			.exchange()
			.expectStatus().isOk
			.expectHeader().contentTypeCompatibleWith("text/plain")
			.expectBody(String::class.java)
			.value { body ->
				val metrics = requireNotNull(body)
				assertTrue(metrics.contains("# HELP jvm_memory_used_bytes "))
				assertTrue(metrics.contains("# TYPE jvm_memory_used_bytes gauge"))
				assertTrue(metrics.lineSequence().any { it.startsWith("jvm_memory_used_bytes{") })
			}
	}

	@ParameterizedTest
	@ValueSource(strings = ["/loggedin-soknad", "/nologin-soknad", "/internal/info", "/internal/metrics", "/internal/prometheus/extra"])
	fun `Other endpoints still require a token`(path: String) {
		client.get().uri(path)
			.exchange()
			.expectStatus().isUnauthorized
	}
}
