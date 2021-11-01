package no.nav.soknad.arkivering.soknadsmottaker.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import no.nav.soknad.arkivering.soknadsmottaker.config.AppConfiguration
import no.nav.soknad.arkivering.soknadsmottaker.dto.SoknadInnsendtDto
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.InetAddress
import java.net.URL
import java.util.*

@Service
class ReSender(private val archiverService: ArchiverService,
							 private val appConfiguration: AppConfiguration
) {
	private val logger = LoggerFactory.getLogger(javaClass)
	private val failedApplications = appConfiguration.reSendList.applicationString

	init {
		val gson = Gson()
		if (!failedApplications.isBlank() && isLeader() ) {
			val myType = object : TypeToken<List<SoknadInnsendtDto>>() {}.type
			val applications = gson.fromJson<List<SoknadInnsendtDto>>(failedApplications, myType)
			// For hver SoknadInnsendtDto send til archiverService
			applications.forEach {s -> logAndSend(s)}
		}
	}

	final fun isLeader(): Boolean {
		val electorPath = System.getenv("ELECTOR_PATH") ?: System.getProperty("ELECTOR_PATH")
		if (electorPath.isNullOrBlank()) return false
		try {
			val jsonString = URL(electorPath).readText()
			val leader = JSONObject(jsonString).getString("name")
			val hostname =
				if (appConfiguration.kafkaConfig.profiles.equals("", true)) "localhost" else InetAddress.getLocalHost().hostName
			return hostname.equals(leader, true)
		} catch (exception: Exception) {
			return false
		}
	}

	private fun logAndSend(application: SoknadInnsendtDto) {
		val key = UUID.randomUUID().toString()
		logger.info("$key: Failed application to be re-sent '${print(application)}'")
		archiverService.archive(key, application)
	}

	private fun print(dto: SoknadInnsendtDto): String {
		val fnrMasked = SoknadInnsendtDto(
			dto.innsendingsId, dto.ettersendelse, "***",
			dto.tema, dto.innsendtDato, dto.innsendteDokumenter
		)
		return fnrMasked.toString()
	}

}
