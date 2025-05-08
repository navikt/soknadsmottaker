package no.nav.soknad.arkivering.soknadsmottaker.rest

import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.soknad.arkivering.soknadsmottaker.SoknadsmottakerApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.EnableTransactionManagement
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.soknad.arkivering.soknadsmottaker.utils.Api
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import com.ninjasquad.springmockk.MockkBean
import io.mockk.*
import io.prometheus.metrics.model.registry.PrometheusRegistry
import no.nav.soknad.arkivering.avroschemas.InnsendingMetrics
import no.nav.soknad.arkivering.avroschemas.Soknadarkivschema
import no.nav.soknad.arkivering.soknadsmottaker.service.KafkaSender
import no.nav.soknad.arkivering.soknadsmottaker.utils.createSoknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus

@ActiveProfiles("test")
@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
	properties = ["spring.main.allow-bean-definition-overriding=true"],
	classes = [SoknadsmottakerApplication::class]
)
@EnableTransactionManagement
@EnableMockOAuth2Server(port = 1888)
@AutoConfigureWireMock(port = 5490)
class RestEndpointTest {

	@Autowired
	lateinit var restTemplate: TestRestTemplate

	@MockBean
	lateinit var prometheusRegistry: PrometheusRegistry

	@Autowired
	lateinit var mockOAuth2Server: MockOAuth2Server

	@MockkBean
	lateinit var oauth2TokenService: OAuth2AccessTokenService

	@MockkBean
	lateinit var kafkaSender: KafkaSender

	@Value("\${server.port}")
	var serverPort: Int? = 8090

	var api: Api? = null

	@BeforeEach
	fun setup() {
		clearAllMocks()
		api = Api(restTemplate, serverPort!!, mockOAuth2Server)
		every { oauth2TokenService.getAccessToken(any()) } returns OAuth2AccessTokenResponse(access_token = "token")
	}

	@Test
	fun `When receiving REST call, message is put on Kafka`() {
		val soknad = createSoknad()
		every { kafkaSender.publishSoknadarkivschema(any(), any()) } returns Unit
		every { kafkaSender.publishMetric(any(), any()) } returns Unit

		val status = api?.receiveSoknad(soknad)

		assertEquals(HttpStatus.OK, status, "Should return HttpStatus.OK")

		val innsendingsIdSlot = slot<String>()
		val soknadSlot = slot<Soknadarkivschema>()

		verify(exactly = 1) { kafkaSender.publishSoknadarkivschema(capture(innsendingsIdSlot), capture(soknadSlot) ) }
		assertTrue(innsendingsIdSlot.isCaptured)
		assertEquals(soknad.innsendingId, innsendingsIdSlot.captured, "Should send correct message")
		assertTrue(soknadSlot.isCaptured)
		assertEquals("BIL", soknadSlot.captured.arkivtema, "Should have correct tema")

		val metricsIdSlot = slot<String>()
		val metricsDataSlot = slot<InnsendingMetrics>()
		verify(exactly = 1) { kafkaSender.publishMetric(capture(metricsIdSlot), capture(metricsDataSlot) ) }
		assertEquals(
			soknad.innsendingId, metricsIdSlot.captured,
			"Metrics should have a correct innsendingsId"
		)
		assertEquals(
			"soknadsmottaker", metricsDataSlot.captured.application,
			"Metrics should have correct application name"
		)

	}

}
