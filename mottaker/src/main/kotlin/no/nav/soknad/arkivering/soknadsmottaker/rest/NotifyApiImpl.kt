package no.nav.soknad.arkivering.soknadsmottaker.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import no.nav.soknad.arkivering.soknadsmottaker.api.NotifyApi
import no.nav.soknad.arkivering.soknadsmottaker.model.AddNotification
import no.nav.soknad.arkivering.soknadsmottaker.model.NotificationInfo
import no.nav.soknad.arkivering.soknadsmottaker.model.Soknad
import no.nav.soknad.arkivering.soknadsmottaker.model.SoknadRef
import no.nav.soknad.arkivering.soknadsmottaker.service.ArchiverService
import no.nav.soknad.arkivering.soknadsmottaker.service.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class NotifyApiImpl (private val notificationService: NotificationService): NotifyApi  {
	private val logger = LoggerFactory.getLogger(javaClass)
	private val secureLogger = LoggerFactory.getLogger("secureLogger")

	/**
	 * The following annotations are copied from [NotifyApiImpl.newApplication].
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
		val soknad = addNotification.soknad!![0]
		val key = soknad.innsendingId
		log(key, soknad)
		val brukerNotifikasjonInfo = addNotification.brukernotifikasjonInfo[0]
		notificationService.newNotification(key, soknad, brukerNotifikasjonInfo)
		return ResponseEntity(HttpStatus.OK)
	}

	/**
	 * The following annotations are copied from [NotifyApiImpl.newApplication].
	 */
	@Operation(
		summary = "Message in order to cancel a published user notification",
		operationId = "cancelNotification",
		description = "After an application is sent in or deleted previous user notification shall be canceld.")
	@ApiResponses(
		value = [ApiResponse(responseCode = "200", description = "Successful operation")])
	@RequestMapping(
		method = [RequestMethod.POST],
		value = ["/notify/done"],
		consumes = ["application/json"]
	)
	override fun cancelNotification(soknad: SoknadRef): ResponseEntity<Unit> {
		val key = soknad.innsendingId
		logger.info("$key: Request to delete notification for deleted Application")
		notificationService.removeNotification(key, soknad)
		return ResponseEntity(HttpStatus.OK)
	}

	private fun log(key: String, soknad: SoknadRef) {
		val fnrMasked = SoknadRef(
			soknad.innsendingId,
			soknad.erEttersendelse,
			"**fnr can be found in secure logs**",
			soknad.groupId,
			soknad.endringsDato
		)
		logger.info("$key: Request to publish notification for new Application '$fnrMasked'")
		secureLogger.info("$key: Request to publish notification for new Application '$soknad'")
	}
}
