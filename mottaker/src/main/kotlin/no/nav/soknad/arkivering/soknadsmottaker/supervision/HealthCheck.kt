package no.nav.soknad.arkivering.soknadsmottaker.supervision

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/internal"])
class HealthCheck {

	@GetMapping("/isAlive")
	fun isAlive() = "Ok"

	@GetMapping("/ping")
	fun ping() = "pong"

	@GetMapping("/isReady")
	fun isReady() = "Ready for actions"
}
