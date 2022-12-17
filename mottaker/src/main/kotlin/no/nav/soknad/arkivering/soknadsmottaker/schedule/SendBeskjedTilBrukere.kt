package no.nav.soknad.arkivering.soknadsmottaker.schedule

import com.google.gson.Gson
import no.nav.soknad.arkivering.soknadsmottaker.dto.UserDto
import no.nav.soknad.arkivering.soknadsmottaker.dto.UserMessageDto
import no.nav.soknad.arkivering.soknadsmottaker.dto.UserNotificationMessageDto
import no.nav.soknad.arkivering.soknadsmottaker.model.NotificationInfo
import no.nav.soknad.arkivering.soknadsmottaker.model.Varsel
import no.nav.soknad.arkivering.soknadsmottaker.service.NotificationService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

@Service
@EnableScheduling
class SendBeskjedTilBrukere(
	private val notificationService: NotificationService,
	private val leaderSelectionUtility: LeaderSelectionUtility
	) {

	val logger: Logger = LoggerFactory.getLogger(javaClass)

	@Value("\${userNotificationMessageJson}")
	private var envInput: String? = null

	@Scheduled(cron = "\${cron.startSendBrukerBeskjed}")
	fun start() {
		val inputString: String? = envInput ?: System.getenv("userNotificationMessage") ?: System.getProperty("userNotificationMessage")
		logger.info("**** Start sending av usernotification, ${if (inputString != null) inputString.length else null} ****")
		try {
			if (leaderSelectionUtility.isLeader() && inputString != null) {

				val gson = Gson()
				val input = gson.fromJson(inputString, UserNotificationMessageDto::class.java)

				input.userList.forEach {
					publiser(
						it,
						if (it.language == "en") input.userMessage_en else input.userMessage,
						input.messageLinkBase
					)
				}
			} else {
				logger.info("**** Sending skipped ****")
			}
		} catch (ex: Exception) {
			logger.warn("Sending av usernotification feilet med ${ex.message}")
		}
	}

	private fun publiser(user: UserDto, message: UserMessageDto, linkBase: String) {
		val key = UUID.randomUUID().toString()
		val link = linkBase + convertSchemaString(user.schema) + "?sub=digital"
		val smsVarsel = Varsel(Varsel.Kanal.sms, message.smsText!! , message.smsTitle)
		val emailVarsel = Varsel(Varsel.Kanal.epost, message.emailText!! , message.emailTitle)
		val notificationInfo = NotificationInfo(notifikasjonsTittel = message.message, link,14, listOf(smsVarsel, emailVarsel) )
		notificationService.userMessageNotification(key, notificationInfo, user.userId, user.innsendingRef)
		logger.info("Har publisert beskjed:\t$key relatert til innsendingsid:\t${user.innsendingRef} ")
	}

	private fun convertSchemaString(schema: String): String {
		return (schema.replace(" ", "").replace(".","").replace("-","")).lowercase()
	}

	fun getBytesFromFile(path: String): ByteArray {
		val resourceAsStream = SendBeskjedTilBrukere::class.java.getResourceAsStream(path)
		val outputStream = ByteArrayOutputStream()
		resourceAsStream.use { input ->
			outputStream.use { output ->
				input!!.copyTo(output)
			}
		}
		return outputStream.toByteArray()
	}

	fun canReadFile(filePath: String): Boolean = File(filePath).canRead()

	fun readeBytesFromFile(filePath: String): ByteArray {
		return File(filePath).readBytes()
	}


}

