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
			logger.info("Antall søknader som må re-sendes=${applications.size}")
			applications.forEach {s -> logAndSend(s)}
		}
	}

	final fun isLeader(): Boolean {
		logger.info("Sjekk om leader")
		val electorPath = System.getenv("ELECTOR_PATH") ?: System.getProperty("ELECTOR_PATH")
		if (electorPath.isNullOrBlank()) {
			logger.info("ELECTOR_PATH er null eller blank")
			return false
		}
		try {
			logger.info("Elector_path=$electorPath")
			val fullUrl = if (electorPath.contains(":/")) electorPath else "http://"+electorPath
			val jsonString = URL(fullUrl).readText()
			logger.info("Elector_path som jsonstring=${jsonString}")
			val leader = JSONObject(jsonString).getString("name")
			val hostname =
				if (appConfiguration.kafkaConfig.profiles.equals("", true)) "localhost" else InetAddress.getLocalHost().hostName
			logger.info("isLeader=${hostname.equals(leader, true)}")
			return hostname.equals(leader, true)
		} catch (exception: Exception) {
			logger.warn("Sjekk om leader feilet med:", exception)
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
