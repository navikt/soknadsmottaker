package no.nav.soknad.arkivering.soknadsmottaker.utils

import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.soknad.arkivering.soknadsmottaker.model.Innsending
import no.nav.soknad.arkivering.soknadsmottaker.model.Soknad
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration

class Api(val restTemplate: WebTestClient, val mockOAuth2Server: MockOAuth2Server) {

	private val BEARER = "Bearer "

	fun createHeaders(issuer: String?, audience: String?, map: Map<String, String>? = mapOf()): HttpHeaders {
		val token = when {
			issuer == null -> null
			issuer == "azuread" -> TokenGenerator(mockOAuth2Server).lagAzureADToken(audience_ = audience)
			issuer == "tokenx"	-> TokenGenerator(mockOAuth2Server).lagTokenXToken()
			else -> null
		}
		val headers = HttpHeaders()
		headers.contentType = MediaType.APPLICATION_JSON
		if (token != null) 	headers.add(HttpHeaders.AUTHORIZATION, "$BEARER$token")
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
			.headers { it.addAll(createHeaders(issuer = "azuread", audience = null)) }
			.bodyValue(soknad)

			.exchange()
			.returnResult()

		return response.status
	}

	fun receiveSoknad(soknad: Soknad, issuer: String? = "azuread", audience: String?): HttpStatusCode {

		val response = restTemplate
			.mutate()
			.responseTimeout(Duration.ofMinutes(2))
			.build()

			.post()
			.uri { uriBuilder -> uriBuilder.path("/soknad").build() }
			.headers { it.addAll(createHeaders(issuer = issuer, audience = audience)) }
			.bodyValue(soknad)

			.exchange()
			.returnResult()

		return response.status
	}



	fun receiveNoLoginSoknad(soknad: Innsending, issuer: String? = "azuread"): HttpStatusCode {

		val response = restTemplate
			.mutate()
			.responseTimeout(Duration.ofMinutes(2))
			.build()

			.post()
			.uri { uriBuilder -> uriBuilder.path("/nologin-soknad").build() }
			.headers { it.addAll(createHeaders(issuer = issuer, audience = null)) }
			.bodyValue(soknad)

			.exchange()
			.returnResult()

		return response.status
	}

}
