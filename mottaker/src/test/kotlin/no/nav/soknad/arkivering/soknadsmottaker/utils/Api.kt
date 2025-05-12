package no.nav.soknad.arkivering.soknadsmottaker.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.soknad.arkivering.soknadsmottaker.model.Soknad
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.web.util.UriComponentsBuilder

class Api(val restTemplate: TestRestTemplate, val serverPort: Int, val mockOAuth2Server: MockOAuth2Server) {

	private val BEARER = "Bearer "

	val baseUrl = "http://localhost:${serverPort}"
	val objectMapper: ObjectMapper = jacksonObjectMapper().findAndRegisterModules()

	private fun <T> createHttpEntity(body: T, map: Map<String, String>? = mapOf()): HttpEntity<T> {
		val token: String = TokenGenerator(mockOAuth2Server).lagTokenXToken()
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
		val headers: Map<String, String>? =  null
		val uri = UriComponentsBuilder.fromHttpUrl("${baseUrl}/soknad")
			.build()
			.toUri()

		val response = restTemplate.exchange(uri, HttpMethod.POST, createHttpEntity(soknad, headers), String::class.java)

		return response.statusCode

	}
}
