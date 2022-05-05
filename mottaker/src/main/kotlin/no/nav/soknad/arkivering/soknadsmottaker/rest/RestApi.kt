package no.nav.soknad.arkivering.soknadsmottaker.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import no.nav.soknad.arkivering.soknadsmottaker.api.SoknadApi
import no.nav.soknad.arkivering.soknadsmottaker.model.Soknad
import no.nav.soknad.arkivering.soknadsmottaker.service.ArchiverService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class RestApi(private val archiverService: ArchiverService, private val testApi: TestApi) : SoknadApi {
	private val logger = LoggerFactory.getLogger(javaClass)
	private val secureLogger = LoggerFactory.getLogger("secureLogger")

	/**
	 * The following annotations are copied from [SoknadApi.receive].
	 */
	@Operation(
		summary = "Receives benefit application",
		operationId = "receive",
		description = "Receives a benefit application, which will lead to it being put on a Kafka topic and archived.")
	@ApiResponses(
		value = [ApiResponse(responseCode = "200", description = "Successful operation")])
	@RequestMapping(
		method = [RequestMethod.POST],
		value = ["/soknad"],
		consumes = ["application/json"]
	)
	override fun receive(soknad: Soknad): ResponseEntity<Unit> {
		val key = soknad.innsendingId
		log(key, soknad)
		compareRequests(key, soknad)
		archiverService.archive(key, soknad)
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

	// Method to verify that the test endpoint and the real endpoint receive identical requests
	private fun compareRequests(key: String, soknad: Soknad) {
		val testSoknad = testApi.receivedRequests[key]
		if (testSoknad == null) {
			logger.warn("$key: Didn't find any received test requests for key $key")
		} else if (testSoknad != soknad) {
			logger.warn("$key: Received test request didn't match!\n$testSoknad\n$soknad")
		} else {
			testApi.receivedRequests.remove(key)
		}
	}
}
