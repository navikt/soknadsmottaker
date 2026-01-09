package no.nav.soknad.arkivering.soknadsmottaker.utils

import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.soknad.arkivering.soknadsmottaker.model.Soknad
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration

class Api(val restTemplate: WebTestClient, val mockOAuth2Server: MockOAuth2Server) {

	private val BEARER = "Bearer "

	private fun <T: Any> createHttpEntity(body: T, map: Map<String, String>? = mapOf()): HttpEntity<T> {
		val token: String = TokenGenerator(mockOAuth2Server).lagAzureADToken()
		return HttpEntity(body, createHeaders(token, map))
	}

	fun createHeaders(token: String, map: Map<String, String>? = mapOf()): HttpHeaders {
		val headers = HttpHeaders()
		headers.contentType = MediaType.APPLICATION_JSON
		headers.add(HttpHeaders.AUTHORIZATION, "$BEARER$token")
		map?.forEach { (headerName, headerValue) -> headers.add(headerName, headerValue) }
		return headers
	}

	fun createHeaders(token: String, contentType: MediaType): HttpHeaders {
		val headers = HttpHeaders()
		headers.contentType = contentType
		headers.add(HttpHeaders.AUTHORIZATION, "$BEARER$token")
		return headers
	}

	fun receiveSoknad(soknad: Soknad): HttpStatusCode {

		val response = restTemplate
			.mutate()
			.responseTimeout(Duration.ofMinutes(2))
			.build()

			.post()
			.uri { uriBuilder -> uriBuilder.path("/soknad").build() }
			.headers { it.addAll(createHeaders(TokenGenerator(mockOAuth2Server).lagAzureADToken())) }
			.bodyValue(soknad)

			.exchange()
			.returnResult()

		return response.status

	}
}
