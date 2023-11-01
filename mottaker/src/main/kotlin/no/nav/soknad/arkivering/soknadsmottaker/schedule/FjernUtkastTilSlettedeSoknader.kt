package no.nav.soknad.arkivering.soknadsmottaker.schedule

import no.nav.soknad.arkivering.soknadsmottaker.service.NotificationService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@EnableScheduling
class FjernUtkastTilSlettedeSoknader(
	private val notificationService: NotificationService,
	private val leaderSelectionUtility: LeaderSelectionUtility
) {
	val logger: Logger = LoggerFactory.getLogger(javaClass)

	@Value("\${cron.fjernGamleUtkast}")
	private var envInput: String? = null

	@Scheduled(cron = "\${cron.fjernGamleUtkast}")
	fun start() {
		if (leaderSelectionUtility.isLeader()) {
			val utkastIds = listOf("e3127252-e111-4924-942d-5a64a188f679")

			utkastIds.forEach { notificationService.publishDoneUtkastNotification(it, "") }
		}

	}


}
