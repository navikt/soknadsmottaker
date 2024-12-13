package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.soknad.arkivering.soknadsmottaker.config.KafkaConfig
import no.nav.soknad.arkivering.soknadsmottaker.model.NotificationInfo
import no.nav.soknad.arkivering.soknadsmottaker.model.SoknadRef
import no.nav.soknad.arkivering.soknadsmottaker.model.Varsel
import no.nav.tms.utkast.builder.UtkastJsonBuilder
import no.nav.tms.varsel.action.*
import no.nav.tms.varsel.builder.VarselActionBuilder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

@Service
class NotificationService(
	private val kafkaSender: KafkaSender,
	private val kafkaConfig: KafkaConfig
) {

	private val logger = LoggerFactory.getLogger(javaClass)

	val allowedNamespace = listOf("team-soknad")

	private val sensitivityLevel = Sensitivitet.High // Forutsetter at brukere har logget seg på f.eks. bankId slik at nivå 4 er oppnådd
	private val sleepTimes = listOf(5, 15, 30)

	fun userMessageNotification(key:String, brukerNotifikasjonInfo: NotificationInfo, userId: String, groupId: String) {
		if (!correctNamespace(kafkaConfig.namespace)) {
			logger.info("$key: Will not publish Beskjed, the namespace '${kafkaConfig.namespace}' is not correct.")
			return
		}
		publishNewNotification(varselType = Varseltype.Beskjed, brukerNotifikasjonInfo = brukerNotifikasjonInfo,
			key = key, eventId = key, fodselsnummer = userId)
	}

	fun selectNotificationTypeAndPublish(key: String, soknad: SoknadRef, brukerNotifikasjonInfo: NotificationInfo) {
		if (!correctNamespace(kafkaConfig.namespace)) {
			logger.info("$key: Will not publish Utkast/Oppgave, the namespace '${kafkaConfig.namespace}' is not correct.")
			return
		}

		logger.info("$key: skal opprette ny notifikasjon")

		//  Når en bruker tar initiativ til å opprette en søknad/ettersending lages det et utkast.
		//  Når systemet ser at det mangler påkrevde vedlegg som skal ettersendes, lages det en oppgave i stedet.
		if (soknad.erSystemGenerert == true) {
			publishNewNotification(varselType = Varseltype.Oppgave,
				brukerNotifikasjonInfo = brukerNotifikasjonInfo, key = key, eventId = key, fodselsnummer = soknad.personId)
		} else {
			publishNewUtkastNotification(brukerNotifikasjonInfo = brukerNotifikasjonInfo, eventId = key, ident = soknad.personId)
		}

	}

	fun cancelNotification(key: String, soknad: SoknadRef) {
		if (!correctNamespace(kafkaConfig.namespace)) {
			logger.info("$key: Will not publish Done Event, the namespace '${kafkaConfig.namespace}' is not correct.")
			return
		}

		// Publiserer done/slett/inaktiver event på både oppgave/beskjed topic OG utkast topic da vi ikke sikkert vet hvor eventen ble publisert.
		publishDoneNotification(key = key)
		publishDoneUtkastNotification(eventId = key)

	}

	private fun publishNewNotification(
		varselType: Varseltype,
		brukerNotifikasjonInfo: NotificationInfo,
		key: String,
		eventId: String,
		fodselsnummer: String
	) {

		val notification = generateNotificationType(
			varseltype = varselType, eventId = eventId,
			persId = fodselsnummer, notificationInfo = brukerNotifikasjonInfo )

		loopAndPublishToKafka(key,varselType.name) {
			logger.info(
				"$key: Varsel om $varselType skal publiseres med eventId=$eventId og med lenke ${brukerNotifikasjonInfo.lenke}"
			)
			when {
				varselType == Varseltype.Oppgave -> {
					kafkaSender.publishOppgaveNotification(key = key, value = notification)
				}
				varselType == Varseltype.Beskjed -> {
					kafkaSender.publishBeskjedNotification(key = key, value = notification)
				}
				else -> {
					logger.warn("$key: Unexpected varselType = $varselType. Will not be published")
				}
			}
		}
	}

	private fun publishNewUtkastNotification(
		brukerNotifikasjonInfo: NotificationInfo,
		eventId: String,
		ident: String
	) {
		logger.info("$eventId: Lager utkast med lenke ${brukerNotifikasjonInfo.lenke}")
		val utkast = UtkastJsonBuilder()
			.withUtkastId(eventId)
			.withLink(brukerNotifikasjonInfo.lenke)
			.withIdent(ident)
			.withTittel(brukerNotifikasjonInfo.notifikasjonsTittel)
			.create()

		try {
			kafkaSender.publishUtkastNotification(eventId, utkast)
			logger.info("$eventId: Publisert utkast med lenke ${brukerNotifikasjonInfo.lenke}")
		} catch ( ex: Exception) {
			logger.error("$eventId: feil ved publisering av utkast med lenke ${brukerNotifikasjonInfo.lenke}, \n${ex.message}")
		}

	}

	fun publishDoneUtkastNotification(
		eventId: String
	) {
		val utkast = UtkastJsonBuilder()
			.withUtkastId(eventId)
			.delete()

		kafkaSender.publishUtkastNotification(eventId, utkast)

	}

	private fun publishDoneNotification(
		key: String,
	) {
		val doneNotification = createDeleteNotification(eventId = key)
		loopAndPublishToKafka(key, "Done") {
			kafkaSender.publishDoneNotification(key, doneNotification)
		}
	}

	private fun loopAndPublishToKafka(
		key: String,
		notificationType: String,
		publishingLambda: () -> Unit
	) {
		for ((index, sleepTime) in sleepTimes.withIndex()) {
			try {
				publishingLambda.invoke()
				break

			} catch (e: Exception) {
				if (index != sleepTimes.size - 1)
					logger.warn("$key: Failed to publish $notificationType notification - will try again in $sleepTime s", e)
				else
					logger.error("$key: Failed to publish $notificationType notification - Giving up", e)
				TimeUnit.SECONDS.sleep(sleepTime.toLong())
			}
		}
	}

	private fun correctNamespace(currentNamespace: String) = allowedNamespace.contains(currentNamespace)

	private fun generateNotificationType(
		varseltype: Varseltype?,
		eventId: String,
		persId: String,
		notificationInfo: NotificationInfo
	): String =
		VarselActionBuilder.opprett {
			type = varseltype
			varselId = eventId
			sensitivitet = sensitivityLevel
			ident = persId
			tekster += Tekst(
				spraakkode = "nb",
				tekst = notificationInfo.notifikasjonsTittel,
				default = true
			)
			link = notificationInfo.lenke
			aktivFremTil = ZonedDateTime.now(ZoneId.of("Z")).plusDays(notificationInfo.antallAktiveDager.toLong())
			if (notificationInfo.eksternVarsling.isNotEmpty()) {
				eksternVarsling {
					preferertKanal =
						notificationInfo.eksternVarsling.map { if (it.kanal == Varsel.Kanal.sms) EksternKanal.SMS else EksternKanal.EPOST }
							.first()
					smsVarslingstekst = notificationInfo.eksternVarsling.firstOrNull { it.kanal == Varsel.Kanal.sms }?.tekst
					epostVarslingstittel =
						notificationInfo.eksternVarsling.firstOrNull { it.kanal == Varsel.Kanal.epost }?.tittel
					epostVarslingstekst = notificationInfo.eksternVarsling.firstOrNull { it.kanal == Varsel.Kanal.epost }?.tekst
					utsettSendingTil = notificationInfo.utsettSendingTil?.toZonedDateTime()
				}
			}
		}

	private fun createDeleteNotification(eventId: String): String =
		VarselActionBuilder.inaktiver { varselId = eventId }

	}
