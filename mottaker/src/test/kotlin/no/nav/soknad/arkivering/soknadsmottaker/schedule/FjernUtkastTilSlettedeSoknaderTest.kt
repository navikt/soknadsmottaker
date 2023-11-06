package no.nav.soknad.arkivering.soknadsmottaker.schedule

import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockk
import no.nav.soknad.arkivering.soknadsmottaker.service.NotificationService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue


class FjernUtkastTilSlettedeSoknaderTest {

	private val notificationService = mockk<NotificationService>(relaxUnitFun = true)
	private val leaderSelectionUtility = mockk<LeaderSelectionUtility>()

	@Test
	fun testLesOgKonverterInput() {
		System.setProperty("fjernGamleUtkastInputFil", "slett-utkast-dev.json")

		val fjernUtkast = FjernUtkastTilSlettedeSoknader(notificationService, leaderSelectionUtility)
		every { leaderSelectionUtility.isLeader() } returns true
		val behandlingIds = mutableListOf<String>()
		every { notificationService.publishDoneUtkastNotification(capture(behandlingIds)) } returns Unit

		fjernUtkast.start()

		assertTrue(behandlingIds.isNotEmpty())

	}
}
