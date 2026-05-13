package no.nav.soknad.arkivering.soknadsmottaker.rest

import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.soknad.arkivering.soknadsmottaker.SoknadsmottakerApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.EnableTransactionManagement
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.soknad.arkivering.soknadsmottaker.utils.Api
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.web.reactive.server.WebTestClient
import com.ninjasquad.springmockk.MockkBean
import io.mockk.*
import io.prometheus.metrics.model.registry.PrometheusRegistry
import no.nav.soknad.arkivering.avroschemas.InnsendingMetrics
import no.nav.soknad.arkivering.soknadsmottaker.model.AvsenderDto
import no.nav.soknad.arkivering.soknadsmottaker.model.BrukerDto
import no.nav.soknad.arkivering.soknadsmottaker.service.KafkaSender
import no.nav.soknad.arkivering.soknadsmottaker.utils.createInnsending
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doReturn

import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean

@ActiveProfiles("default")
@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = ["spring.main.allow-bean-definition-overriding=true"],
	classes = [SoknadsmottakerApplication::class]
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
	lateinit var kafkaSender: KafkaSender

	@Value("\${server.port}")
	var serverPort: Int? = 8090

	var api: Api? = null

	private val AUD = "aud-localhost"

	private val AZURE_ISSUER = "http://localhost:1888/azuread"
	private val TOKENX_ISSUER = "http://localhost:1888/tokenx"


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
	fun `When receiving REST call to nologin endpoint, message is put on Kafka`() {
		// Given
		val brukerId = "01234567891"
		val avsenderId = "12345678901"
		val soknad = createInnsending(
			brukerDto = BrukerDto(brukerId, BrukerDto.IdType.FNR),
			avsenderDto = AvsenderDto(
				id = avsenderId,
				idType = AvsenderDto.IdType.FNR,
				navn = null
			), kanal = "NAV_NO_UINNLOGGET", tema = "BIL"
		)
		every { kafkaSender.publishSubmission(key= any(), value=any(), loggedIn=any()) } returns Unit
		every { kafkaSender.publishMetric(any(), any()) } returns Unit
		val mockJwt = createMockJwt(AZURE_ISSUER)
		(doReturn(mockJwt).`when` (azureJwtDecoder).decode(any()))

		// When
		val status = api?.receiveNoLoginSoknad(soknad)

		// Expect
		assertEquals(HttpStatus.OK, status, "Should return HttpStatus.OK")

		val innsendingsIdSlot = slot<String>()
		val innsendingMsg = slot<String>()
		val isLoggedInSlot = slot<Boolean>()

		verify(exactly = 1) { kafkaSender.publishSubmission(key=capture(innsendingsIdSlot), value=capture(innsendingMsg), loggedIn=capture(isLoggedInSlot) ) }
		assertTrue(innsendingsIdSlot.isCaptured)
		assertEquals(soknad.innsendingsId, innsendingsIdSlot.captured, "Should send correct message")
		assertTrue(innsendingMsg.isCaptured)
		assertTrue( innsendingMsg.captured.contains("BIL"), "Should have correct tema")
		assertTrue( innsendingMsg.captured.contains("NAV_NO_UINNLOGGET"), "Should have correct kanal")
		assertTrue(isLoggedInSlot.isCaptured)
		assertEquals( false, isLoggedInSlot.captured, "Should send isLoggedIn==false")

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
	fun `When receiving REST call to loggedin endpoint, message is put on Kafka`() {
		// Given
		val brukerId = "01234567891"
		val avsenderId = "12345678901"
		val soknad = createInnsending(
			brukerDto = BrukerDto(brukerId, BrukerDto.IdType.FNR),
			avsenderDto = AvsenderDto(
				id = avsenderId,
				idType = AvsenderDto.IdType.FNR,
				navn = null
			), kanal = "NAV_NO", tema = "BIL"
		)
		every { kafkaSender.publishSubmission(key= any(), value=any(), loggedIn=any()) } returns Unit
		every { kafkaSender.publishMetric(any(), any()) } returns Unit
		val mockJwt = createMockJwt(AZURE_ISSUER)
		(doReturn(mockJwt).`when` (azureJwtDecoder).decode(any()))

		// When
		val status = api?.receiveLoggedinSoknad(soknad)

		// Expect
		assertEquals(HttpStatus.OK, status, "Should return HttpStatus.OK")

		val innsendingsIdSlot = slot<String>()
		val innsendingMsg = slot<String>()
		val isLoggedInSlot = slot<Boolean>()

		verify(exactly = 1) { kafkaSender.publishSubmission(key=capture(innsendingsIdSlot), value=capture(innsendingMsg), loggedIn=capture(isLoggedInSlot) ) }
		assertTrue(innsendingsIdSlot.isCaptured)
		assertEquals(soknad.innsendingsId, innsendingsIdSlot.captured, "Should send correct message")
		assertTrue(innsendingMsg.isCaptured)
		assertTrue( innsendingMsg.captured.contains("BIL"), "Should have correct tema")
		assertTrue( innsendingMsg.captured.contains("NAV_NO"), "Should have correct kanal")
		assertTrue(isLoggedInSlot.isCaptured)
		assertEquals( true, isLoggedInSlot.captured, "Should send isLoggedIn==true")

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
		// Given
		val brukerId = "01234567891"
		val avsenderId = "12345678901"
		val soknad = createInnsending(
			brukerDto = BrukerDto(brukerId, BrukerDto.IdType.FNR),
			avsenderDto = AvsenderDto(
				id = avsenderId,
				idType = AvsenderDto.IdType.FNR,
				navn = null
			), kanal = "NAV_NO", tema = "BIL"
		)
		every { kafkaSender.publishSubmission(key= any(), value=any(), loggedIn=any()) } returns Unit
		every { kafkaSender.publishMetric(any(), any()) } returns Unit
		val mockJwt = createMockJwt(AZURE_ISSUER)
		(doReturn(null).`when` (azureJwtDecoder).decode(any()))

		// When
		val status = api?.receiveLoggedinSoknad(soknad, issuer = null)

		// Expect
		assertEquals(HttpStatus.UNAUTHORIZED, status, "Should return HttpStatus.UNAUTHORIZED")

	}


	@Test
	fun `When receiving REST call with tokenx token, message is rejected`() {
		// Given
		val brukerId = "01234567891"
		val avsenderId = "12345678901"
		val soknad = createInnsending(
			brukerDto = BrukerDto(brukerId, BrukerDto.IdType.FNR),
			avsenderDto = AvsenderDto(
				id = avsenderId,
				idType = AvsenderDto.IdType.FNR,
				navn = null
			), kanal = "NAV_NO", tema = "BIL"
		)
		every { kafkaSender.publishSubmission(key= any(), value=any(), loggedIn=any()) } returns Unit
		every { kafkaSender.publishMetric(any(), any()) } returns Unit
		val mockJwt = createMockJwt(TOKENX_ISSUER)
		(doReturn(mockJwt).`when` (azureJwtDecoder).decode(any()))

		// When
		val status = api?.receiveLoggedinSoknad(soknad, issuer = "tokenx", audience = AUD)

		 // Expect
		assertEquals(HttpStatus.UNAUTHORIZED, status, "Should return HttpStatus.UNAUTHORIZED")

	}


	@Test
	fun `When receiving REST call with token with wrong audience, message is rejected`() {
		// Given
		val brukerId = "01234567891"
		val avsenderId = "12345678901"
		val soknad = createInnsending(
			brukerDto = BrukerDto(brukerId, BrukerDto.IdType.FNR),
			avsenderDto = AvsenderDto(
				id = avsenderId,
				idType = AvsenderDto.IdType.FNR,
				navn = null
			), kanal = "NAV_NO", tema = "BIL"
		)
		every { kafkaSender.publishSubmission(key= any(), value=any(), loggedIn=any()) } returns Unit
		every { kafkaSender.publishMetric(any(), any()) } returns Unit
		val mockJwt = createMockJwt(issuer = AZURE_ISSUER, audience = "wrongAudience")
		(doReturn(mockJwt).`when` (azureJwtDecoder).decode(any()))

		// When
		val status = api?.receiveLoggedinSoknad(soknad, issuer = "azuread", audience = "wrongAudience")

		// Expect
		assertEquals(HttpStatus.UNAUTHORIZED, status, "Should return HttpStatus.UNAUTHORIZED")

	}

}
