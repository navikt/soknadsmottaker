package no.nav.soknad.arkivering.soknadsmottaker.schedule

import no.nav.soknad.arkivering.soknadsmottaker.model.SoknadRef
import no.nav.soknad.arkivering.soknadsmottaker.service.NotificationService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

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
			val utkastIds = listOf("eb0ac392-9ba9-4e68-91f9-cdd7f9081c45")

			utkastIds.forEach { notificationService.cancelNotification(it, SoknadRef(it, false, it, "", OffsetDateTime.now(), false)) }
		}

	}


}
