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
import org.springframework.test.web.reactive.server.WebTestClient
import com.ninjasquad.springmockk.MockkBean
import io.mockk.*
import org.mockito.Mockito.`when`
import io.prometheus.metrics.model.registry.PrometheusRegistry
import no.nav.soknad.arkivering.avroschemas.InnsendingMetrics
import no.nav.soknad.arkivering.avroschemas.Soknadarkivschema
import no.nav.soknad.arkivering.soknadsmottaker.service.KafkaSender
import no.nav.soknad.arkivering.soknadsmottaker.utils.createSoknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.cache.annotation.EnableCaching
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("itest")
@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = ["spring.main.allow-bean-definition-overriding=true"],
	classes = [SoknadsmottakerApplication::class]
)
@ExtendWith(
	SpringExtension::class
)
@EnableTransactionManagement
@EnableMockOAuth2Server(port = 1888)
@AutoConfigureWebTestClient
class RestEndpointTest {

	@MockitoBean
	protected lateinit var azureJwtDecoder: JwtDecoder

	@Autowired
	lateinit var restTemplate: WebTestClient

	@MockitoBean
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

	private val AUD = "aud-localhost"

	private val AZURE_ISSUER = "http://localhost:1888/azuread"

	@BeforeEach
	fun setup() {
		clearAllMocks()
		api = Api(restTemplate, mockOAuth2Server)
		every { oauth2TokenService.getAccessToken(any()) } returns OAuth2AccessTokenResponse(access_token = "token")
	}

	private fun createMockJwt(issuer: String): Jwt {
		// returnere en Jwt som har en tokenValue som er en gyldig JWT-streng fra MockOAuth2Server.
		val token = mockOAuth2Server.issueToken(issuerId = "azuread", audience = AUD)

		return Jwt.withTokenValue(token.serialize())
			.header("alg", "RS256")
			.claim("iss", issuer)
			.claim("aud", AUD)
			.build()
	}

	@Test
	fun `When receiving REST call, message is put on Kafka`() {
		val soknad = createSoknad()
		every { kafkaSender.publishSoknadarkivschema(any(), any()) } returns Unit
		every { kafkaSender.publishMetric(any(), any()) } returns Unit
		val mockJwt = createMockJwt(AZURE_ISSUER)
		`when`(azureJwtDecoder.decode(anyString())).thenReturn(mockJwt)

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
