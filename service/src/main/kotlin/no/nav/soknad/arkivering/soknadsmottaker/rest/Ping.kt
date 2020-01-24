package no.nav.soknad.arkivering.soknadsmottaker.rest

import io.swagger.annotations.Api
import io.swagger.annotations.Authorization
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Api(authorizations = [
	(Authorization(value = "Basic"))
], tags = ["soknadsmottaker"])
@RestController
class Ping {
	private val logger = LoggerFactory.getLogger(javaClass)

	@GetMapping("/internal/ping")
	fun ping(): String {
		logger.info("pinget")
		return "pong"
	}

	@GetMapping("/internal/isAlive")
	fun isAlive(): String {
		logger.info("isAlive er kalt")
		return "Ok"
	}

}
