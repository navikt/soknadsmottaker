package no.nav.soknad.arkivering.soknadsmottaker.rest

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import jakarta.validation.Valid
import no.nav.security.token.support.core.api.Protected
import no.nav.soknad.arkivering.soknadsmottaker.api.NologinSoknadApi
import no.nav.soknad.arkivering.soknadsmottaker.model.Innsending
import no.nav.soknad.arkivering.soknadsmottaker.service.InnsendingService
import no.nav.soknad.arkivering.soknadsmottaker.util.maskIdsInInnsending
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@Controller
class NologinSubmission(private val innsendingService: InnsendingService): NologinSoknadApi {

	private val logger = LoggerFactory.getLogger(javaClass)
	private val secureLogsMarker = MarkerFactory.getMarker("TEAM_LOGS")

	@Protected
	override fun nologinSubmission(
		@Parameter(description = "Metadata about the benefit application being sent in.", required = true) @Valid @RequestBody innsending: Innsending,
		@Parameter(description = "Tracing id that will be used in logging statements.", `in` = ParameterIn.HEADER) @RequestHeader(value = "X-innsendingId", required = false) xInnsendingId: String?
	): ResponseEntity<Unit> {

		log(innsending.innsendingsId, innsending)
		innsendingService.publishToNoLoginTopic(innsending.innsendingsId, innsending )

		return ResponseEntity(HttpStatus.OK)
	}

	private fun log(key: String, soknad: Innsending) {
		logger.info("$key: Received request ${maskIdsInInnsending(soknad)}")
		logger.info(secureLogsMarker, "$key: Received request '$soknad'")
	}

}
