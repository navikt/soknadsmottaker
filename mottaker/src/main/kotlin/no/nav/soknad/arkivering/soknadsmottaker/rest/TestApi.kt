package no.nav.soknad.arkivering.soknadsmottaker.rest

import no.nav.soknad.arkivering.soknadsmottaker.api.SoknadTestApi
import no.nav.soknad.arkivering.soknadsmottaker.model.Soknad
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class TestApi : SoknadTestApi {
	private val logger = LoggerFactory.getLogger(javaClass)
	private val secureLogger = LoggerFactory.getLogger("secureLogger")

	override fun receiveTest(soknad: Soknad): ResponseEntity<Unit> {
		val key = soknad.innsendingId
		log(key, soknad)

		return ResponseEntity(HttpStatus.OK)
	}

	private fun log(key: String, soknad: Soknad) {
		val fnrMasked = Soknad(
			soknad.innsendingId,
			soknad.erEttersendelse,
			personId = "**fnr can be found in secure logs**",
			soknad.tema,
			soknad.dokumenter
		)
		logger.info("$key: TEST ENDPOINT - Received request '$fnrMasked'")
		secureLogger.info("$key: TEST ENDPOINT - Received request '$soknad'")
	}
}
