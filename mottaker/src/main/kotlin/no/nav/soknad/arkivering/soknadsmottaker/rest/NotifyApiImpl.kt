package no.nav.soknad.arkivering.soknadsmottaker.rest

import no.nav.security.token.support.core.api.Protected
import no.nav.soknad.arkivering.soknadsmottaker.api.NotifyApi
import no.nav.soknad.arkivering.soknadsmottaker.model.AddNotification
import no.nav.soknad.arkivering.soknadsmottaker.model.SoknadRef
import no.nav.soknad.arkivering.soknadsmottaker.service.NotificationService
import no.nav.soknad.arkivering.soknadsmottaker.util.Constants
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class NotifyApiImpl(private val notificationService: NotificationService) : NotifyApi {
	private val logger = LoggerFactory.getLogger(javaClass)
	private val secureLogger = LoggerFactory.getLogger("secureLogger")


	@Protected
	override fun newNotification(addNotification: AddNotification, xDryRun: String?): ResponseEntity<Unit> {
		val soknadRef = addNotification.soknadRef
		val key = soknadRef.innsendingId
		MDC.put(Constants.MDC_INNSENDINGS_ID, key)
		log(key, "Request to publish message or task notification for", soknadRef)

		if (xDryRun.isDryRunEnabled()) {
			logger.info("{}: DryRun enabled - will not create new Notification", key)
			return ResponseEntity(HttpStatus.OK)
		}

		notificationService.newNotification(key, soknadRef, addNotification.brukernotifikasjonInfo)
		return ResponseEntity(HttpStatus.OK)
	}

	@Protected
	override fun cancelNotification(soknadRef: SoknadRef, xDryRun: String?): ResponseEntity<Unit> {
		val key = soknadRef.innsendingId
		MDC.put(Constants.MDC_INNSENDINGS_ID, key)
		log(key, "Request to publish done notification for", soknadRef)

		if (xDryRun.isDryRunEnabled()) {
			logger.info("{}: DryRun enabled - will not create cancel Notification", key)
			return ResponseEntity(HttpStatus.OK)
		}

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

	private fun String?.isDryRunEnabled() = this != null
}
