package no.nav.soknad.arkivering.soknadsmottaker.schedule

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper
import java.net.InetAddress
import java.net.URL
import tools.jackson.module.kotlin.jacksonObjectMapper


@Component
class LeaderSelectionUtility() {
	val logger = LoggerFactory.getLogger(javaClass)

	@Autowired
	private val objectMapper: ObjectMapper = jacksonObjectMapper()

	fun isLeader(): Boolean {
		val hostname = InetAddress.getLocalHost().hostName
		val jsonString = fetchLeaderSelection()
		val leader = objectMapper.readValue(jsonString, object: TypeReference<LeaderElection>(){}).name

		val isLeader = hostname.equals(leader, true)
		logger.info("isLeader=$isLeader")
		return isLeader
	}

	fun fetchLeaderSelection(): String {
		val electorPath = System.getenv("ELECTOR_PATH") ?: System.getProperty("ELECTOR_PATH")
		if (electorPath.isNullOrBlank()) {
			logger.info("ELECTOR_PATH er null eller blank")
			throw RuntimeException("ELECTOR_PATH er null eller blank")
		}
		logger.info("Elector_path=$electorPath")
		val fullUrl = if (electorPath.contains(":/")) electorPath else "http://$electorPath"
		val jsonString = URL(fullUrl).readText()
		logger.info("Elector_path som jsonstring=$jsonString")
		return jsonString
	}

}

