package no.nav.soknad.arkivering.soknadsmottaker.rest

import no.nav.security.token.support.core.api.Protected
import no.nav.soknad.arkivering.soknadsmottaker.api.NotifyApi
import no.nav.soknad.arkivering.soknadsmottaker.model.AddNotification
import no.nav.soknad.arkivering.soknadsmottaker.model.SoknadRef
import no.nav.soknad.arkivering.soknadsmottaker.service.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class NotifyApiImpl(private val notificationService: NotificationService) : NotifyApi {
	private val logger = LoggerFactory.getLogger(javaClass)
	private val secureLogger = LoggerFactory.getLogger("secureLogger")

	private fun String?.isDryRunEnabled() = this != null

	@Protected
	override fun newNotification(addNotification: AddNotification, xDryRun: String?): ResponseEntity<Unit> {
		if (xDryRun.isDryRunEnabled()) {
			logger.info("{}: DryRun enabled - will not create new Notification", addNotification.soknadRef)
			return ResponseEntity(HttpStatus.OK)
		}
		val soknadRef = addNotification.soknadRef
		val key = soknadRef.innsendingId
		log(key, "Request to publish message or task notification for", soknadRef)
		val brukerNotifikasjonInfo = addNotification.brukernotifikasjonInfo
		notificationService.newNotification(key, soknadRef, brukerNotifikasjonInfo)


		return ResponseEntity(HttpStatus.OK)
	}

	@Protected
	override fun cancelNotification(soknadRef: SoknadRef, xDryRun: String?): ResponseEntity<Unit> {
		val key = soknadRef.innsendingId
		if (xDryRun.isDryRunEnabled()) {
			logger.info("{}: DryRun enabled - will not create cancel Notification", key)
			return ResponseEntity(HttpStatus.OK)
		}
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
