package no.nav.soknad.arkivering.soknadsmottaker.utils

import com.nimbusds.jose.JOSEObjectType
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.token.support.spring.test.MockLoginController


class TokenGenerator(
	private val mockOAuth2Server: MockOAuth2Server,
) {
	companion object {
		val subject = "12345678901"
	}

	private val tokenx = "tokenx"
	private val audience = "aud-localhost"
	private val expiry = 2 * 3600L

	fun lagTokenXToken(fnr: String? = null): String {
		val pid = fnr ?: subject
		return mockOAuth2Server.issueToken(
			issuerId = tokenx,
			clientId = "application",
			tokenCallback = DefaultOAuth2TokenCallback(
				issuerId = tokenx,
				subject = pid,
				typeHeader = JOSEObjectType.JWT.type,
				audience = listOf(audience),
				claims = mapOf("acr" to "idporten-loa-high", "pid" to pid),
				expiry = expiry
			)
		).serialize()
	}

}
