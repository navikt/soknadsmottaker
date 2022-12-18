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

	@Test
	fun lagBase64Encoded() {
		/*
				val userNotificationMessage = readeBytesFromFile("userNotificationMessage-1-kopi.json")

				val encodedString: String = Base64.getEncoder().encodeToString(userNotificationMessage)
		*/

				val gson = Gson()
/*
				val input = gson.fromJson(Base64.getDecoder().decode(encodedString).decodeToString(), UserNotificationMessageDto::class.java)

				val test = Base64.getDecoder().decode(encodedString).decodeToString()
*/
		val userNotificationMessageDto = lagInput()
		val jsonByteArray = gson.toJson(userNotificationMessageDto).encodeToByteArray()
		val encodedString = Base64.getEncoder().encodeToString(jsonByteArray)
		val importedEncodedString = "eyJ1c2VyTWVzc2FnZSI6eyJtZXNzYWdlIjoiRHUgbcOlIHNlbmRlIHPDuGtuYWRlbiBww6Ugbnl0dC4gVmkgaGFyIGhhdHQgZW4gdGVrbmlzayBmZWlsIHNvbSBnam9yZGUgYXQgZGV0IGlra2UgdmFyIG11bGlnIMOlIHNlbmRlIGlubiB2ZWRsZWdnIHRpbCBzw7hrbmFkZXIgZnJhIDEyLiB0aWwgMTQuIGRlc2VtYmVyLiBGZWlsZW4gZ2plbGRlciBkZXNzdmVycmUgZW4gc8O4a25hZCBkdSBoYXIgc2VuZHQgaW5uLiBGb3IgYXQgTkFWIHNrYWwga3VubmUgYmVoYW5kbGUgc8O4a25hZGVuIGRpbiBtw6UgZHUgc2VuZGUgc8O4a25hZCBvZyB2ZWRsZWdnIHDDpSBueXR0LiIsInNtc1RpdGxlIjoiRGV0IGVyIGVuIGJlc2tqZWQgdGlsIGRlZyBww6UgbmF2Lm5vIiwic21zVGV4dCI6IlZlbm5saWdzdCBsb2dnIGlubiBvZyBzamVrayBiZXNramVkZW4iLCJlbWFpbFRpdGxlIjoiRGV0IGVyIGVuIGJlc2tqZWQgdGlsIGRlZyBww6UgbmF2Lm5vIiwiZW1haWxUZXh0IjoiVmVubmxpZ3N0IGxvZ2cgaW5uIG9nIHNqZWtrIGJlc2tqZWRlbiJ9LCJ1c2VyTWVzc2FnZV9lbiI6eyJtZXNzYWdlIjoiQ3JlYXRlIG5ldyBBcHBsaWNhdGlvbiIsInNtc1RpdGxlIjoiVGhlcmUgaXMgaXMgYSBtZXNzYWdlIHRvIHlvdSwgc2VlIG5hdi5uby9taW5zaWRlIiwic21zVGV4dCI6IlBsZWFzZSBsb2cgb24gdG8gY2hlY2sgdGhlIG1lc3NhZ2UiLCJlbWFpbFRpdGxlIjoiVGhlcmUgaXMgaXMgYSBtZXNzYWdlIHRvIHlvdSwgc2VlIG5hdi5uby9taW5zaWRlIiwiZW1haWxUZXh0IjoiUGxlYXNlIGxvZyBvbiB0byBjaGVjayB0aGUgbWVzc2FnZSJ9LCJtZXNzYWdlTGlua0Jhc2UiOiJodHRwczovL3NramVtYWRlbGluZ3NsZW5rZS5la3N0ZXJuLmRldi5uYXYubm8vZnlsbHV0LyIsInVzZXJMaXN0IjpbeyJpbm5zZW5kaW5nUmVmIjoiNGFlZTM1OWEtYTZiNy00NzJkLWJjNzEtNzBkZjkyYTU2NDJkIiwidXNlcklkIjoiMTQ4NzgwOTk0MzYiLCJzY2hlbWEiOiJOQVYgMDgtMzUuMDEiLCJsYW5ndWFnZSI6Im5vIn0seyJpbm5zZW5kaW5nUmVmIjoiNTNhMmI5ZDYtYWU4Yi00Mjc0LWJmYzItZDFkMjBlMDkyNzhmIiwidXNlcklkIjoiMjM4Mjg4OTY3NDEiLCJzY2hlbWEiOiJOQVYgMDQtMDIuMDEiLCJsYW5ndWFnZSI6ImVuIn1dfQ=="
		val importedDecodedString = String(Base64.getDecoder().decode(importedEncodedString.replace("SEND_MESSAGE_TO_USERS=","")))
		val input = gson.fromJson(String(Base64.getDecoder().decode(encodedString)), UserNotificationMessageDto::class.java)
		val importedInput = gson.fromJson(importedDecodedString, UserNotificationMessageDto::class.java)
		val substring1 = encodedString.substring(0,encodedString.length)
		val substring2 = importedEncodedString.substring(0,encodedString.length)
		assertEquals(input, userNotificationMessageDto)
		assertEquals(importedInput, userNotificationMessageDto)
		assertEquals(substring1, substring2)


	}


	fun writeBytesToFile(data: ByteArray, filePath: String) {
		File(filePath).writeBytes(data)
	}

	fun readeBytesFromFile(filePath: String): ByteArray {
		return File(filePath).readBytes()
	}

}
