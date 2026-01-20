package no.nav.soknad.arkivering.soknadsmottaker.utils

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo

object WireMockStubs {

	fun stubTokenEndpoint(expectedResponse: String = "mocked-token") {
		stubFor(
			post(urlEqualTo("/azuread"))
				.willReturn(
					aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody(expectedResponse)
				)
		)
	}

}
