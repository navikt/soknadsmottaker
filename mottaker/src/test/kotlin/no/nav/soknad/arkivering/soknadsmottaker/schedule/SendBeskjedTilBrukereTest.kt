package no.nav.soknad.arkivering.soknadsmottaker.schedule

import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockk
import no.nav.soknad.arkivering.soknadsmottaker.dto.UserDto
import no.nav.soknad.arkivering.soknadsmottaker.dto.UserMessageDto
import no.nav.soknad.arkivering.soknadsmottaker.dto.UserNotificationMessageDto
import no.nav.soknad.arkivering.soknadsmottaker.model.NotificationInfo
import no.nav.soknad.arkivering.soknadsmottaker.service.NotificationService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*

class SendBeskjedTilBrukereTest {

	private val notificationService = mockk<NotificationService>(relaxUnitFun = true)
	private val leaderSelectionUtility = mockk<LeaderSelectionUtility>()
	private val sourceFile = "user-notification-message"


	fun lagInput(): UserNotificationMessageDto {
		val userDto = UserDto("4aee359a-a6b7-472d-bc71-70df92a5642d", "14878099436", "NAV 08-35.01","no")
		val userDto2 = UserDto("53a2b9d6-ae8b-4274-bfc2-d1d20e09278f", "23828896741", "NAV 04-02.01", "en")
		val userMessageDto = UserMessageDto("Du må sende søknaden på nytt. Vi har hatt en teknisk feil som gjorde at det ikke var mulig å sende inn vedlegg til søknader fra 12. til 14. desember. Feilen gjelder dessverre en søknad du har sendt inn. For at NAV skal kunne behandle søknaden din må du sende søknad og vedlegg på nytt.", "Det er en beskjed til deg på nav.no", "Vennligst logg inn og sjekk beskjeden",
			"Det er en beskjed til deg på nav.no", "Vennligst logg inn og sjekk beskjeden")
		val userMessageDto_en = UserMessageDto("Create new Application", "There is is a message to you, see nav.no/minside", "Please log on to check the message",
			"There is is a message to you, see nav.no/minside", "Please log on to check the message")
		val userNotificationMessageDto = UserNotificationMessageDto(userMessageDto, userMessageDto_en,
			"https://skjemadelingslenke.ekstern.dev.nav.no/fyllut/", listOf(userDto, userDto2)
		)

		return userNotificationMessageDto

	}

	@AfterEach
	fun cleanUp() {
		File(sourceFile).delete()
	}

	@Test
	fun testLesOgKonverterInput() {
		val sendBeskjedTilBrukere = SendBeskjedTilBrukere(notificationService, leaderSelectionUtility)
		val userNotificationMessageDto = lagInput()
		val filePath = ""

		val gson = Gson()

		val jsonString = gson.toJson(userNotificationMessageDto)
		val encodedJsonString: String = Base64.getEncoder().encodeToString(jsonString.toByteArray())

		writeBytesToFile(jsonString.toByteArray(Charsets.UTF_8), filePath+sourceFile)

		System.setProperty("userNotificationMessage", encodedJsonString)

		every { leaderSelectionUtility.isLeader() } returns true
		val brukernotifikasjonInfos = mutableListOf<NotificationInfo>()
		every { notificationService.userMessageNotification(any(),capture(brukernotifikasjonInfos), any(), any()) } returns Unit

		sendBeskjedTilBrukere.start()

		assertTrue(brukernotifikasjonInfos.isNotEmpty())

	}



	fun writeBytesToFile(data: ByteArray, filePath: String) {
		File(filePath).writeBytes(data)
	}

	fun readeBytesFromFile(filePath: String): ByteArray {
		return File(filePath).readBytes()
	}


	@Test
	fun lagBase64Encoded() {
		/*
		Denne testen benyttes for å generere en Base64encoded string som kan legges inn som value i google secret.
		 */
		val jsonByteArray = readeBytesFromFile("userNotificationMessage-noUsers.json")

		val encodedString: String = Base64.getEncoder().encodeToString(jsonByteArray)
		val gson = Gson()

		val input = gson.fromJson(String(Base64.getDecoder().decode(encodedString)), UserNotificationMessageDto::class.java)

		assertEquals("https://www.nav.no/fyllut/", input.messageLinkBase)

	}

/*
	@Test
	fun findUsersWithMissingZero() {
		/*
			Denne testen skal normalt utkommentert. Brukt for å fikse på fødselsnummeret som var blitt feil.
			Kan kanskje brukes som et utgangspunkt for annen type oppbygging av brukerliste når det er behov for sending av varsel.
		 */
		val jsonByteArray = readeBytesFromFile("userNotificationMessage.json")
		val jsonString = jsonByteArray.decodeToString()

		val gson = Gson()

		val userNotificationMessageDto = gson.fromJson(jsonString, UserNotificationMessageDto::class.java)

		val userList: List<UserDto> = userNotificationMessageDto.userList.filter{it.userId.length==10}.map { UserDto(it.innsendingRef,"0"+it.userId, it.schema, it.language) }

		val fixedNotificationMessages = UserNotificationMessageDto(userNotificationMessageDto.userMessage, userNotificationMessageDto.userMessage_en, userNotificationMessageDto.messageLinkBase, userList)

		val fixedJsonString = gson.toJson(fixedNotificationMessages)

		writeBytesToFile(fixedJsonString.toByteArray(Charsets.UTF_8), "userNotificationMessage-fixed.json")

	}
*/


}
