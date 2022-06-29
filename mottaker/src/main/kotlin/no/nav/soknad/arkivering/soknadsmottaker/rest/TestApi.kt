package no.nav.soknad.arkivering.soknadsmottaker.rest

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.soknad.arkivering.soknadsfillager.api.FilesApi
import no.nav.soknad.arkivering.soknadsfillager.infrastructure.ApiClient
import no.nav.soknad.arkivering.soknadsfillager.infrastructure.ClientException
import no.nav.soknad.arkivering.soknadsfillager.infrastructure.Serializer
import no.nav.soknad.arkivering.soknadsmottaker.api.SoknadTestApi
import no.nav.soknad.arkivering.soknadsmottaker.config.FileStorageProperties
import no.nav.soknad.arkivering.soknadsmottaker.model.Soknad
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class TestApi(fileStorageProperties: FileStorageProperties) : SoknadTestApi {
	private val logger = LoggerFactory.getLogger(javaClass)
	private val secureLogger = LoggerFactory.getLogger("secureLogger")

	private val seenValues = hashMapOf<String, Soknad>()

	private val filesApi: FilesApi

	init {
		Serializer.jacksonObjectMapper.registerModule(JavaTimeModule())
		ApiClient.username = fileStorageProperties.username
		ApiClient.password = fileStorageProperties.password
		filesApi = FilesApi(fileStorageProperties.host)
	}


	override fun receiveTest(soknad: Soknad, xInnsendingId: String?, xOriginSystem: String?): ResponseEntity<Unit> {
		val key = xInnsendingId ?: soknad.innsendingId
		log(key, soknad)

		checkSoknadsfillager(xInnsendingId, xOriginSystem, soknad)
		updateOrCompareValues(xInnsendingId, key, soknad)

		return ResponseEntity(HttpStatus.OK)
	}

	private fun checkSoknadsfillager(xInnsendingId: String?, xOriginSystem: String?, soknad: Soknad) {
		if (xOriginSystem != null && xOriginSystem == "sendsoknad") {
			val fileIds: List<String> = soknad.dokumenter.flatMap { it.varianter.map { variant -> variant.id } }
			try {
				filesApi.checkFilesByIdsTest(fileIds, xInnsendingId)
				logger.info("$xInnsendingId: TEST ENDPOINT - All files present in Soknadsfillager")

			} catch (e: ClientException) {
				if (e.statusCode == 404)
					logger.warn("$xInnsendingId: TEST ENDPOINT - No files present in Soknadsfillager")
				if (e.statusCode == 409)
					logger.warn("$xInnsendingId: TEST ENDPOINT - Only some files present in Soknadsfillager")
			}
		}
	}

	@Synchronized
	private fun updateOrCompareValues(xInnsendingId: String?, key: String, soknad: Soknad) {
		if (xInnsendingId != null && xInnsendingId.startsWith("henvendelse_")) {
			val id = xInnsendingId.replace("henvendelse_", "")
			if (id in seenValues) {
				val soknadFromSendsoknad = seenValues[id]!!
				if (soknad == soknadFromSendsoknad) {
					logger.info("$id: Soknader are equal")
					seenValues.remove(id)
				} else {
					logger.warn("$id: Soknader differs!\nSS: ${maskFnr(soknadFromSendsoknad)}\nHV: ${maskFnr(soknad)}")
				}
			} else {
				logger.warn("$id: Never seen Soknad before\n${maskFnr(soknad)}")
			}
		} else {
			seenValues[key] = soknad
		}
	}

	private fun log(key: String, soknad: Soknad) {
		logger.info("$key: TEST ENDPOINT - Received request '${maskFnr(soknad)}'")
		secureLogger.info("$key: TEST ENDPOINT - Received request '$soknad'")
	}

	private fun maskFnr(soknad: Soknad) = Soknad(
		soknad.innsendingId,
		soknad.erEttersendelse,
		personId = "**fnr can be found in secure logs**",
		soknad.tema,
		soknad.dokumenter
	)
}
