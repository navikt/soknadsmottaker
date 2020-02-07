package no.nav.soknad.arkivering.soknadsmottaker.supervise

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class IsAlive() {

	@GetMapping("/internal/isAlive")
	fun isAlive(): String {
		return "Ok"
	}

	@GetMapping("/internal/ping")
	fun ping(): String {
		return "pong"
	}

}
