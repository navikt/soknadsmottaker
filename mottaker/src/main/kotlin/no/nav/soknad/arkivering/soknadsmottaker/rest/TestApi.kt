package no.nav.soknad.arkivering.soknadsmottaker.rest

import no.nav.soknad.arkivering.soknadsmottaker.api.SoknadTestApi
import no.nav.soknad.arkivering.soknadsmottaker.model.Soknad
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class TestApi : SoknadTestApi {
	private val logger = LoggerFactory.getLogger(javaClass)
	private val secureLogger = LoggerFactory.getLogger("secureLogger")

	private val seenValues = hashMapOf<String, Soknad>()

	override fun receiveTest(soknad: Soknad, xInnsendingId: String?): ResponseEntity<Unit> {
		val key = xInnsendingId ?: soknad.innsendingId
		log(key, soknad)

		updateOrCompareValues(xInnsendingId, key, soknad)

		return ResponseEntity(HttpStatus.OK)
	}

	@Synchronized
	private fun updateOrCompareValues(xInnsendingId: String?, key: String, soknad: Soknad) {
		if (xInnsendingId != null && xInnsendingId.startsWith("henvendelse_")) {
			val id = xInnsendingId.replace("henvendelse_", "")
			if (id in seenValues) {
				val soknadFromSendsoknad = seenValues[id]!!
				if (soknad == soknadFromSendsoknad) {
					logger.info("$id: Soknader are equal")
					seenValues.remove(id)
				} else {
					logger.warn("$id: Soknader differs!\nSS: ${maskFnr(soknadFromSendsoknad)}\nHV: ${maskFnr(soknad)}")
				}
			} else {
				logger.warn("$id: Never seen Soknad before\n${maskFnr(soknad)}")
			}
		} else {
			seenValues[key] = soknad
		}
	}

	private fun log(key: String, soknad: Soknad) {
		logger.info("$key: TEST ENDPOINT - Received request '${maskFnr(soknad)}'")
		secureLogger.info("$key: TEST ENDPOINT - Received request '$soknad'")
	}

	private fun maskFnr(soknad: Soknad) = Soknad(
		soknad.innsendingId,
		soknad.erEttersendelse,
		personId = "**fnr can be found in secure logs**",
		soknad.tema,
		soknad.dokumenter
	)
}
