package no.nav.soknad.arkivering.soknadsmottaker.utils

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo

object WireMockStubs {

	fun stubTokenEndpoint() {
		stubFor(
			post(urlEqualTo("/fake/token"))
				.willReturn(
					aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody(
							"""
                            {
                              "access_token": "mocked-token",
                              "token_type": "Bearer",
                              "expires_in": 3600
                            }
                            """.trimIndent()
						)
				)
		)
	}

}
