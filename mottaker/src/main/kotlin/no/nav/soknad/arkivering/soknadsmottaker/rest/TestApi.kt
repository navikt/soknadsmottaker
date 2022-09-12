package no.nav.soknad.arkivering.soknadsmottaker.rest

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.security.token.support.core.api.Protected
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



	@Protected
	override fun receiveTest(soknad: Soknad, xInnsendingId: String?, xOriginSystem: String?): ResponseEntity<Unit> {

		return ResponseEntity(HttpStatus.OK)
	}


}
