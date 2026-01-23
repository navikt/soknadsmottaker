package no.nav.soknad.arkivering.soknadsmottaker.rest

import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.soknad.arkivering.soknadsmottaker.SoknadsmottakerApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.EnableTransactionManagement
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.soknad.arkivering.soknadsmottaker.utils.Api
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.web.reactive.server.WebTestClient
import com.ninjasquad.springmockk.MockkBean
import io.mockk.*
import io.prometheus.metrics.model.registry.PrometheusRegistry
import no.nav.soknad.arkivering.avroschemas.InnsendingMetrics
import no.nav.soknad.arkivering.avroschemas.Soknadarkivschema
import no.nav.soknad.arkivering.soknadsmottaker.service.KafkaSender
import no.nav.soknad.arkivering.soknadsmottaker.utils.createInnsending
import no.nav.soknad.arkivering.soknadsmottaker.utils.createSoknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doReturn

import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.context.junit.jupiter.SpringExtension

@ActiveProfiles("test")
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

	@MockitoSpyBean
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
	private val TOKENX_ISSUER = "http://localhost:1888/tokenx"


	companion object {
		@JvmField
		@RegisterExtension
		val wm: WireMockExtension = WireMockExtension.newInstance()
			.configureStaticDsl(true)
			.options(
				wireMockConfig()
					.port(1888)
					.notifier(ConsoleNotifier(true))
					.withRootDirectory("src/test/resources")
					.asynchronousResponseEnabled(false)
			)
			.build()

		@JvmStatic
		@DynamicPropertySource
		fun properties(reg: DynamicPropertyRegistry) {
			val base = "http://localhost:1888"

			reg.add("spring.security.auth.issuers.azuread.issuer-uri") { "$base/azuread/.well-known/openid-configuration" }
		}
	}


	@BeforeEach
	fun setup() {
		clearAllMocks()
		api = Api(restTemplate, mockOAuth2Server)
	}

	private fun createMockJwt(issuer: String, audience: String? = AUD): Jwt {
		// returnere en Jwt som har en tokenValue som er en gyldig JWT-streng fra MockOAuth2Server.
		val token = mockOAuth2Server.issueToken(issuerId = "azuread", audience = audience)

		return Jwt.withTokenValue(token.serialize())
			.header("alg", "RS256")
			.claim("iss", issuer)
			.claim("aud", listOf(audience))
			.build()
	}

	@Test
	fun `When receiving REST call, message is put on Kafka`() {
		val soknad = createSoknad()
		every { kafkaSender.publishSoknadarkivschema(any(), any()) } returns Unit
		every { kafkaSender.publishMetric(any(), any()) } returns Unit
		val mockJwt = createMockJwt(AZURE_ISSUER)
		(doReturn(mockJwt).`when` (azureJwtDecoder).decode(any()))

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

	@Test
	fun `When receiving REST call to nologin endpoint, message is put on Kafka`() {
		val soknad = createInnsending()
		every { kafkaSender.publishSoknadarkivschema(any(), any()) } returns Unit
		every { kafkaSender.publishNologinSubmission(any(), any()) } returns Unit
		every { kafkaSender.publishMetric(any(), any()) } returns Unit
		val mockJwt = createMockJwt(AZURE_ISSUER)
		(doReturn(mockJwt).`when` (azureJwtDecoder).decode(any()))

		val status = api?.receiveNoLoginSoknad(soknad)

		assertEquals(HttpStatus.OK, status, "Should return HttpStatus.OK")

		val innsendingsIdSlot = slot<String>()
		val innsendingMsg = slot<String>()

		verify(exactly = 1) { kafkaSender.publishNologinSubmission(capture(innsendingsIdSlot), capture(innsendingMsg) ) }
		assertTrue(innsendingsIdSlot.isCaptured)
		assertEquals(soknad.innsendingsId, innsendingsIdSlot.captured, "Should send correct message")
		assertTrue(innsendingMsg.isCaptured)
		assertTrue( innsendingMsg.captured.contains("BIL"), "Should have correct tema")

		val metricsIdSlot = slot<String>()
		val metricsDataSlot = slot<InnsendingMetrics>()
		verify(exactly = 1) { kafkaSender.publishMetric(capture(metricsIdSlot), capture(metricsDataSlot) ) }
		assertEquals(
			soknad.innsendingsId, metricsIdSlot.captured,
			"Metrics should have a correct innsendingsId"
		)
		assertEquals(
			"soknadsmottaker", metricsDataSlot.captured.application,
			"Metrics should have correct application name"
		)

	}

	@Test
	fun `When receiving REST call without token, message is rejected`() {
		val soknad = createSoknad()
		every { kafkaSender.publishSoknadarkivschema(any(), any()) } returns Unit
		every { kafkaSender.publishMetric(any(), any()) } returns Unit
		(doReturn(null).`when` (azureJwtDecoder).decode(any()))

		val status = api?.receiveSoknad(soknad, issuer = null, audience = null)

		assertEquals(HttpStatus.UNAUTHORIZED, status, "Should return HttpStatus.OK")

	}


	@Test
	fun `When receiving REST call with tokenx token, message is rejected`() {
		val soknad = createSoknad()
		every { kafkaSender.publishSoknadarkivschema(any(), any()) } returns Unit
		every { kafkaSender.publishMetric(any(), any()) } returns Unit
		val mockJwt = createMockJwt(TOKENX_ISSUER)

		(doReturn(mockJwt).`when` (azureJwtDecoder).decode(any()))

		val status = api?.receiveSoknad(soknad, "tokenx", audience = AUD)

		assertEquals(HttpStatus.UNAUTHORIZED, status, "Should return HttpStatus.OK")

	}


	@Test
	fun `When receiving REST call with token with wrong audience, message is rejected`() {
		val soknad = createSoknad()
		every { kafkaSender.publishSoknadarkivschema(any(), any()) } returns Unit
		every { kafkaSender.publishMetric(any(), any()) } returns Unit

		val mockJwt = createMockJwt(issuer = AZURE_ISSUER, audience = "wrongAudience")
		(doReturn(mockJwt).`when` (azureJwtDecoder).decode(any()))

		val status = api?.receiveSoknad(soknad = soknad, issuer = "azuread", audience = "wrongAudience")

		assertEquals(HttpStatus.UNAUTHORIZED, status, "Should return HttpStatus.OK")

	}

}
