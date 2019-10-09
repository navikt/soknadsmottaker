package no.nav.soknad.archiving.archiverproducer.rest

import no.nav.soknad.archiving.archiverproducer.service.ArchiverService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
class Receiver(private val archiverService: ArchiverService) {
	private val logger = LoggerFactory.getLogger(javaClass)

	@PostMapping("/save")
	fun receiveMessage(@RequestBody message: String) {
		val msg = message.replace("=", "")
		logger.info("Received message '$msg'")
		archiverService.archive(msg)
	}
}
