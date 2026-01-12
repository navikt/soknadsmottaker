package no.nav.soknad.arkivering.soknadsmottaker.supervision

import no.nav.soknad.arkivering.soknadsmottaker.api.HealthApi
import no.nav.soknad.arkivering.soknadsmottaker.model.ApplicationStatus
import no.nav.soknad.arkivering.soknadsmottaker.model.ApplicationStatusType
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class HealthCheck(@Value("\${status_log_url}") private val logUrl: String) : HealthApi {

	override fun isAlive() = ResponseEntity<Unit>(HttpStatus.OK)

	override fun ping() = ResponseEntity<Unit>(HttpStatus.OK)

	override fun isReady() = ResponseEntity<Unit>(HttpStatus.OK)

	override fun getStatus(): ResponseEntity<ApplicationStatus> {
		return ResponseEntity(
			ApplicationStatus(status = ApplicationStatusType.OK, description = "OK", logLink = logUrl),
			HttpStatus.OK
		)
	}
}
