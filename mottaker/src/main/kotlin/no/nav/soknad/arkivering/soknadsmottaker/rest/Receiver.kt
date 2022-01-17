package no.nav.soknad.arkivering.soknadsmottaker.rest

import io.micrometer.core.annotation.Timed
import no.nav.soknad.arkivering.soknadsmottaker.dto.SoknadInnsendtDto
import no.nav.soknad.arkivering.soknadsmottaker.service.ArchiverService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@Timed(value = "soknadsmottaker_restcontroller", percentiles = [0.5, 0.95])
class Receiver(private val archiverService: ArchiverService) {
	private val logger = LoggerFactory.getLogger(javaClass)

	@PostMapping("/save")
	fun receiveMessage(
		@RequestHeader("innsendingKey") innsendingKey: String?,
		@RequestBody request: SoknadInnsendtDto
	) {

		val key = getOrMakeKey(innsendingKey)
		logger.info("$key: Received request '${print(request)}'")
		archiverService.archive(key, request)
	}

	private fun getOrMakeKey(innsendingKey: String?): String {
		return if (innsendingKey == null) {
			val key = UUID.randomUUID().toString()
			logger.debug("$key: Did not receive an innsendingKey, generated a new one instead.")
			key
		} else
			innsendingKey
	}


	private fun print(dto: SoknadInnsendtDto): String {
		val fnrMasked = SoknadInnsendtDto(
			dto.innsendingsId, dto.ettersendelse, "***",
			dto.tema, dto.innsendtDato, dto.innsendteDokumenter
		)
		return fnrMasked.toString()
	}
}
