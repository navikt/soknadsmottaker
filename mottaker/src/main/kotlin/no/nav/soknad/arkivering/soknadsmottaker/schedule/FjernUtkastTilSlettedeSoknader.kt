package no.nav.soknad.arkivering.soknadsmottaker.schedule

import com.google.gson.Gson
import no.nav.soknad.arkivering.soknadsmottaker.dto.UserNotificationMessageDto
import no.nav.soknad.arkivering.soknadsmottaker.model.SoknadRef
import no.nav.soknad.arkivering.soknadsmottaker.service.NotificationService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.File
import java.time.OffsetDateTime
import java.util.*

@Service
@EnableScheduling
class FjernUtkastTilSlettedeSoknader(
	private val notificationService: NotificationService,
	private val leaderSelectionUtility: LeaderSelectionUtility
) {
	val logger: Logger = LoggerFactory.getLogger(javaClass)

	@Value("\${cron.fjernGamleUtkastInputFil}")
	private var envInput: String? = null

	@Scheduled(cron = "\${cron.fjernGamleUtkast}")
	fun start() {
		val inputString: String? = envInput ?: System.getenv("fjernGamleUtkastInputFil") ?: System.getProperty("fjernGamleUtkastInputFil")
		if (leaderSelectionUtility.isLeader() && inputString != null) {

			val utkastIds = try {
				getApplicationIdsFromFile("slett-utkast-dev.json")
			} catch (ex: Exception) {
				logger.warn("Feil ved henting av fil med liste med søknader der utkast notifikasjoner skal slettes", ex)
				return
			}

			utkastIds.forEach { notificationService.publishDoneUtkastNotification(it) }
		}

	}

	private fun getApplicationIdsFromFile(filePath: String): List<String> {
		val jsonString = readeBytesFromFile(filePath)

		val gson = Gson()
		val input = gson.fromJson((jsonString), BehandlingIds::class.java)

		return input.ids
	}
	fun readeBytesFromFile(filePath: String): String {
		this::class.java.classLoader
			.getResourceAsStream(filePath).use { inputStream ->
				assert(inputStream != null)
				val s: Scanner = Scanner(inputStream!!).useDelimiter("\\A")
				val json = if (s.hasNext()) s.next() else ""
				assert(json != "")
				return json
			}
	}

	data class BehandlingIds(
		val ids: List<String>
	)
}
