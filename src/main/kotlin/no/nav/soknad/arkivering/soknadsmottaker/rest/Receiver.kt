package no.nav.soknad.arkivering.soknadsmottaker.rest

import no.nav.soknad.arkivering.dto.SoknadInnsendtDto
import no.nav.soknad.arkivering.soknadsmottaker.service.ArchiverService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class Receiver(private val archiverService: ArchiverService) {
	private val logger = LoggerFactory.getLogger(javaClass)

	@PostMapping("/save")
	fun receiveMessage(@RequestBody request: SoknadInnsendtDto) {
		logger.info("Received request '$request'")
		archiverService.archive(request)
	}
}
