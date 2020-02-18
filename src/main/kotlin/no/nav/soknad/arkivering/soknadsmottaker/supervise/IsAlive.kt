package no.nav.soknad.arkivering.soknadsmottaker.supervise

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class IsAlive() {
	private val logger = LoggerFactory.getLogger(javaClass)

	@GetMapping("/internal/isAlive")
	fun isAlive(): String {
		logger.info("isAlive kalt")
		return "Ok"
	}

	@GetMapping("/internal/ping")
	fun ping(): String {
		return "pong"
	}

}
