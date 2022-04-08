package no.nav.soknad.arkivering.soknadsmottaker.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import no.nav.soknad.arkivering.soknadsmottaker.api.NotifyApi
import no.nav.soknad.arkivering.soknadsmottaker.model.AddNotification
import no.nav.soknad.arkivering.soknadsmottaker.model.SoknadRef
import no.nav.soknad.arkivering.soknadsmottaker.service.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class NotifyApiImpl(private val notificationService: NotificationService) : NotifyApi {
	private val logger = LoggerFactory.getLogger(javaClass)
	private val secureLogger = LoggerFactory.getLogger("secureLogger")

	/**
	 * The following annotations are copied from [NotifyApi.newNotification].
	 */
	@Operation(
		summary = "Message in order to publish user notification",
		operationId = "newNotification",
		description = "When creating an application, an user notification with link to the application shall be published.")
	@ApiResponses(
		value = [ApiResponse(responseCode = "200", description = "Successful operation")])
	@RequestMapping(
		method = [RequestMethod.POST],
		value = ["/notify/new"],
		consumes = ["application/json"]
	)
	override fun newNotification(addNotification: AddNotification): ResponseEntity<Unit> {
		val soknadRef = addNotification.soknadRef
		val key = soknadRef.innsendingId
		log(key, "Request to publish message or task notification for", soknadRef)
		val brukerNotifikasjonInfo = addNotification.brukernotifikasjonInfo
		notificationService.newNotification(key, soknadRef, brukerNotifikasjonInfo)
		return ResponseEntity(HttpStatus.OK)
	}

	/**
	 * The following annotations are copied from [NotifyApi.cancelNotification].
	 */
	@Operation(
		summary = "Message in order to cancel a published user notification",
		operationId = "cancelNotification",
		description = "After an application is sent in or deleted previous user notification shall be canceled.")
	@ApiResponses(
		value = [ApiResponse(responseCode = "200", description = "Successful operation")])
	@RequestMapping(
		method = [RequestMethod.POST],
		value = ["/notify/done"],
		consumes = ["application/json"]
	)
	override fun cancelNotification(soknadRef: SoknadRef): ResponseEntity<Unit> {
		val key = soknadRef.innsendingId
		log(key, "Request to publish done notification for", soknadRef)
		notificationService.cancelNotification(key, soknadRef)
		return ResponseEntity(HttpStatus.OK)
	}

	private fun log(key: String, message: String, soknad: SoknadRef) {
		val fnrMasked = SoknadRef(
			soknad.innsendingId,
			soknad.erEttersendelse,
			soknad.groupId,
			personId = "**fnr can be found in secure logs**",
			soknad.tidpunktEndret
		)
		logger.info("$key: $message '$fnrMasked'")
		secureLogger.info("$key: $message '$soknad'")
	}
}
