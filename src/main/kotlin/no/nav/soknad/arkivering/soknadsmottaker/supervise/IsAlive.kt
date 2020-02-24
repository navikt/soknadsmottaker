package no.nav.soknad.arkivering.soknadsmottaker.supervise

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/internal"])
class IsAlive() {
	private val logger = LoggerFactory.getLogger(javaClass)

	@GetMapping("/isAlive")
	fun isAlive(): String {
		logger.info("isAlive kalt")
		return "Ok"
	}

	@GetMapping("/ping")
	fun ping(): String {
		return "pong"
	}

	@GetMapping("/isReady")
	fun isReady(): String {
		logger.info("isReady kalt")
		return "Ready for actions"
	}

}
