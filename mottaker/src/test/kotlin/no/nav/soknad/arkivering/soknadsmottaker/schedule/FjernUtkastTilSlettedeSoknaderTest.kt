package no.nav.soknad.arkivering.soknadsmottaker.schedule

import io.mockk.every
import io.mockk.mockk
import no.nav.soknad.arkivering.soknadsmottaker.service.NotificationService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue

class FjernUtkastTilSlettedeSoknaderTest {

	private val notificationService = mockk<NotificationService>(relaxUnitFun = true)
	private val leaderSelectionUtility = mockk<LeaderSelectionUtility>()

	@Test
	fun testAvslutningAvNotifikasjoner_fullListe() {
		System.setProperty("fjernGamleUtkastInputFil", "slett-utkast.json")

		val fjernUtkast = FjernUtkastTilSlettedeSoknader(notificationService, leaderSelectionUtility)
		every { leaderSelectionUtility.isLeader() } returns true
		val behandlingIds = mutableListOf<String>()
		every { notificationService.publishDoneUtkastNotification(capture(behandlingIds)) } returns Unit

		fjernUtkast.start()

		assertTrue(behandlingIds.isNotEmpty())

	}

	@Test
	fun testAvslutningAvNotifikasjoner_tomListe() {
		System.setProperty("fjernGamleUtkastInputFil", "empty-slett-utkast.json")

		val fjernUtkast = FjernUtkastTilSlettedeSoknader(notificationService, leaderSelectionUtility)
		every { leaderSelectionUtility.isLeader() } returns true
		val behandlingIds = mutableListOf<String>()
		every { notificationService.publishDoneUtkastNotification(capture(behandlingIds)) } returns Unit

		fjernUtkast.start()

		assertTrue(behandlingIds.isEmpty())

	}

}
