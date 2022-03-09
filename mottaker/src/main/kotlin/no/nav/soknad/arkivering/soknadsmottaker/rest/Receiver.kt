package no.nav.soknad.arkivering.soknadsmottaker.rest

import io.micrometer.core.annotation.Timed
import no.nav.soknad.arkivering.soknadsmottaker.dto.InnsendtDokumentDto
import no.nav.soknad.arkivering.soknadsmottaker.dto.InnsendtVariantDto
import no.nav.soknad.arkivering.soknadsmottaker.dto.SoknadInnsendtDto
import no.nav.soknad.arkivering.soknadsmottaker.model.DocumentData
import no.nav.soknad.arkivering.soknadsmottaker.model.Soknad
import no.nav.soknad.arkivering.soknadsmottaker.model.Varianter
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

	@Deprecated("Replaced in favour of OpenAPI generated API code",
		replaceWith = ReplaceWith("RestApi.receive()"))
	@PostMapping("/save")
	fun receiveMessage(
		@RequestHeader("innsendingKey") innsendingKey: String?,
		@RequestBody request: SoknadInnsendtDto
	) {

		val key = getOrMakeKey(innsendingKey)
		logger.info("$key: Received legacy request '${print(request)}'")
		val soknad = convertToSoknad(request)
		archiverService.archive(key, soknad)
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

fun convertToSoknad(request: SoknadInnsendtDto) =
	Soknad(request.innsendingsId, request.ettersendelse, request.personId, request.tema,
		convertDocuments(request.innsendteDokumenter))

fun convertDocuments(list: List<InnsendtDokumentDto>) = list.map { convertDocument(it) }
fun convertDocument(document: InnsendtDokumentDto) = DocumentData(
	document.skjemaNummer,
	document.erHovedSkjema ?: false,
	document.tittel ?: "UNTITLED",
	convertVarianter(document.varianter)
)

fun convertVarianter(list: List<InnsendtVariantDto>) = list.map { convertVariant(it) }
fun convertVariant(varianter: InnsendtVariantDto) = Varianter(
	varianter.uuid,
	varianter.mimeType ?: "UNSET",
	varianter.filNavn ?: "NO_TITLE",
	varianter.filtype
)
