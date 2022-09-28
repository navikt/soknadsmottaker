package no.nav.soknad.arkivering.soknadsmottaker.rest

import no.nav.security.token.support.core.api.Protected
import no.nav.soknad.arkivering.soknadsmottaker.api.SoknadApi
import no.nav.soknad.arkivering.soknadsmottaker.model.Soknad
import no.nav.soknad.arkivering.soknadsmottaker.service.ArchiverService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class RestApi(private val archiverService: ArchiverService) : SoknadApi {
	private val logger = LoggerFactory.getLogger(javaClass)
	private val secureLogger = LoggerFactory.getLogger("secureLogger")

	@Protected
	override fun receive(soknad: Soknad, xDryRun: String?): ResponseEntity<Unit> {
		val key = soknad.innsendingId
		log(key, soknad)

		if (xDryRun == null) {
			archiverService.archive(key, soknad)
		} else {
			logger.info("{}: DryRun enabled - will not archive", soknad.innsendingId)
		}

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
		logger.info("$key: Received request '$fnrMasked'")
		secureLogger.info("$key: Received request '$soknad'")
	}
}
