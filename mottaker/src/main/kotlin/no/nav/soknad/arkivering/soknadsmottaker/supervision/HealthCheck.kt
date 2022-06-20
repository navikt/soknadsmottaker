package no.nav.soknad.arkivering.soknadsmottaker.supervision

import no.nav.soknad.arkivering.soknadsmottaker.api.HealthApi
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class HealthCheck : HealthApi {

	override fun isAlive() = ResponseEntity<Unit>(HttpStatus.OK)

	override fun ping() = ResponseEntity<Unit>(HttpStatus.OK)

	override fun isReady() = ResponseEntity<Unit>(HttpStatus.OK)
}
