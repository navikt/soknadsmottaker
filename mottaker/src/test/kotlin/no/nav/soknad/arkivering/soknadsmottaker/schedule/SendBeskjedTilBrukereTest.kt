package no.nav.soknad.arkivering.soknadsmottaker.schedule

import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.soknad.arkivering.soknadsmottaker.dto.UserDto
import no.nav.soknad.arkivering.soknadsmottaker.dto.UserMessageDto
import no.nav.soknad.arkivering.soknadsmottaker.dto.UserNotificationMessageDto
import no.nav.soknad.arkivering.soknadsmottaker.model.AddNotification
import no.nav.soknad.arkivering.soknadsmottaker.model.NotificationInfo
import no.nav.soknad.arkivering.soknadsmottaker.service.NotificationService
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class SendBeskjedTilBrukereTest {

	private val notificationService = mockk<NotificationService>(relaxUnitFun = true)
	private val leaderSelectionUtility = mockk<LeaderSelectionUtility>()

	fun lagInput(): UserNotificationMessageDto {
		val userDto = UserDto("4aee359a-a6b7-472d-bc71-70df92a5642d", "14878099436", "NAV 08-35.01","no")
		val userDto2 = UserDto("53a2b9d6-ae8b-4274-bfc2-d1d20e09278f", "23828896741", "NAV 04-02.01", "en")
		val userMessageDto = UserMessageDto("Opprett ny søknad", "Det er en beskjed til deg på nav.no/minside", "Vennligst logg inn og sjekk meldingen",
			"Det er en beskjed til deg på nav.no/minside", "Vennligst logg inn og sjekk meldingen")
		val userMessageDto_en = UserMessageDto("Create new Application", "There is is a message to you, see nav.no/minside", "Please log on to check the message",
			"There is is a message to you, see nav.no/minside", "Please log on to check the message")
		val userNotificationMessageDto = UserNotificationMessageDto(userMessageDto, userMessageDto_en,
			"https://skjemadelingslenke.ekstern.dev.nav.no/fyllut/", listOf(userDto, userDto2)
		)

		return userNotificationMessageDto

	}

	@Test
	fun testLesOgKonverterInput() {
		val sendBeskjedTilBrukere = SendBeskjedTilBrukere(notificationService, leaderSelectionUtility)
		val userNotificationMessageDto = lagInput()
		val filePath = "./src/test/resources"
		val sourceFile = "/userNotificationMessage.json"

		val gson = Gson()

		val jsonString = gson.toJson(userNotificationMessageDto)

		writeBytesToFile(jsonString.toByteArray(Charsets.UTF_8), filePath+sourceFile)

		every { leaderSelectionUtility.isLeader() } returns true
		val brukernotifikasjonInfos = mutableListOf<NotificationInfo>()
		every { notificationService.userMessageNotification(any(),capture(brukernotifikasjonInfos), any(), any()) } returns Unit

		sendBeskjedTilBrukere.start()

		assertTrue(brukernotifikasjonInfos.isNotEmpty())

	}

	fun writeBytesToFile(data: ByteArray, filePath: String) {
		File(filePath).writeBytes(data)
	}

}
