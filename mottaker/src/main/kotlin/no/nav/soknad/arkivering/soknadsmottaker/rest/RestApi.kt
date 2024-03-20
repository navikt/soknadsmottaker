package no.nav.soknad.arkivering.soknadsmottaker.rest

import no.nav.security.token.support.core.api.Protected
import no.nav.soknad.arkivering.soknadsmottaker.api.SoknadApi
import no.nav.soknad.arkivering.soknadsmottaker.model.DocumentData
import no.nav.soknad.arkivering.soknadsmottaker.model.Soknad
import no.nav.soknad.arkivering.soknadsmottaker.service.ArchiverService
import no.nav.soknad.arkivering.soknadsmottaker.util.Constants.MDC_INNSENDINGS_ID
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class RestApi(private val archiverService: ArchiverService) : SoknadApi {
	private val logger = LoggerFactory.getLogger(javaClass)
	private val secureLogger = LoggerFactory.getLogger("secureLogger")

	@Protected
	override fun receive(soknad: Soknad, xInnsendingId: String?): ResponseEntity<Unit> {
		val key = soknad.innsendingId
		MDC.put(MDC_INNSENDINGS_ID, key)
		log(key, soknad)

		archiverService.archive(key, soknad)
		return ResponseEntity(HttpStatus.OK)
	}

	private fun log(key: String, soknad: Soknad) {
		val fnrMasked = Soknad(
			soknad.innsendingId,
			soknad.erEttersendelse,
			personId = "**fnr can be found in secure logs**",
			soknad.tema,
			maskDocumentTitle(soknad.dokumenter)
		)
		logger.info("$key: Received request '$fnrMasked'")
		secureLogger.info("$key: Received request '$soknad'")
	}

	private fun maskDocumentTitle(documents:List<DocumentData>): List<DocumentData> {
		return documents.map{DocumentData(it.skjemanummer, it.erHovedskjema, if (it.skjemanummer == "N6") "**Maskert**" else it.tittel, it.varianter)}
	}

}
