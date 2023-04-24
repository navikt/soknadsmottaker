package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.brukernotifikasjon.schemas.builders.BeskjedInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.DoneInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.NokkelInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.OppgaveInputBuilder
import no.nav.brukernotifikasjon.schemas.input.BeskjedInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.brukernotifikasjon.schemas.input.OppgaveInput
import no.nav.soknad.arkivering.soknadsmottaker.config.KafkaConfig
import no.nav.soknad.arkivering.soknadsmottaker.model.NotificationInfo
import no.nav.soknad.arkivering.soknadsmottaker.model.SoknadRef
import no.nav.soknad.arkivering.soknadsmottaker.model.Varsel
import no.nav.soknad.arkivering.soknadsmottaker.model.Varsel.Kanal.epost
import no.nav.soknad.arkivering.soknadsmottaker.model.Varsel.Kanal.sms
import no.nav.tms.utkast.builder.UtkastJsonBuilder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

@Service
class NotificationService(
	private val kafkaSender: KafkaSender,
	private val kafkaConfig: KafkaConfig
) {

	private val logger = LoggerFactory.getLogger(javaClass)

	private val defaultVarselTittel = "Notifikasjon fra NAV"
	val appNavn = "soknadsmottaker"
	val allowedNamespace = listOf("team-soknad")
	val notificationNamespace = "min-side."

	private val securityLevel = 4 // Forutsetter at brukere har logget seg på f.eks. bankId slik at nivå 4 er oppnådd
	private val sleepTimes = listOf(5, 15, 30)


	fun userMessageNotification(key:String, brukerNotifikasjonInfo: NotificationInfo, userId: String, groupId: String) {
		val notifikasjonsNokkel = createNotificationKey(key, userId, groupId)
		publishBeskjedNotification(brukerNotifikasjonInfo, LocalDateTime.now(), key, key, notifikasjonsNokkel)
	}

	fun newNotification(key: String, soknad: SoknadRef, brukerNotifikasjonInfo: NotificationInfo) {
		if (!correctNamespace(kafkaConfig.namespace)) {
			logger.info("$key: Will not publish Beskjed/Oppgave, the namespace '${kafkaConfig.namespace}' is not correct.")
			return
		}

		val eventId = if (isIdFromHenvendelse(key)) createULIDEventId(key, soknad.erEttersendelse) else	key
		val notifikasjonsNokkel = createNotificationKey(eventId, soknad.personId, soknad.groupId)
		val hendelsestidspunkt = soknad.tidpunktEndret.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()

		logger.info("$key: skal opprette ny notifikasjon")

		//  Når en bruker tar initiativ til å opprette en søknad/ettersending lages det et utkast.
		//  Når systemet ser at det mangler påkrevde vedlegg som skal ettersendes, lages det en oppgave i stedet.
		if (soknad.erSystemGenerert == true) {
			publishOppgaveNotification(brukerNotifikasjonInfo, hendelsestidspunkt, key, eventId, notifikasjonsNokkel)
		} else {
			publishNewUtkastNotification(brukerNotifikasjonInfo, notifikasjonsNokkel.eventId, notifikasjonsNokkel.fodselsnummer)
		}

	}

	fun cancelNotification(key: String, soknad: SoknadRef) {
		if (!correctNamespace(kafkaConfig.namespace)) {
			logger.info("$key: Will not publish Done Event, the namespace '${kafkaConfig.namespace}' is not correct.")
			return
		}

		val eventId = if (isIdFromHenvendelse(key)) createULIDEventId(key, soknad.erEttersendelse) else	key
		val notifikasjonsNokkel = createNotificationKey(eventId, soknad.personId, soknad.groupId)
		val hendelsestidspunkt = soknad.tidpunktEndret.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()

		publishDoneNotification(key, hendelsestidspunkt, notifikasjonsNokkel)
		publishDoneUtkastNotification(eventId, soknad.personId)

	}

	private fun publishOppgaveNotification(
		brukerNotifikasjonInfo: NotificationInfo,
		hendelsestidspunkt: LocalDateTime,
		key: String,
		eventId: String,
		notifikasjonsNokkel: NokkelInput
	) {
		val oppgaveNotifikasjon = nyOppgaveNotifikasjon(
			brukerNotifikasjonInfo.notifikasjonsTittel,
			brukerNotifikasjonInfo.lenke,
			hendelsestidspunkt,
			brukerNotifikasjonInfo.antallAktiveDager,
			brukerNotifikasjonInfo.eksternVarsling
		)

		val notificationType = "Oppgave"
		loopAndPublishToKafka(key, notificationType) {
			logger.info(
				"$key: Varsel om $notificationType skal publiseres med eventId=$eventId og med lenke " +
					brukerNotifikasjonInfo.lenke
			)
			kafkaSender.publishOppgaveNotification(notifikasjonsNokkel, oppgaveNotifikasjon)
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

	private fun publishDoneUtkastNotification(
		eventId: String,
		ident: String
	) {
		val utkast = UtkastJsonBuilder()
			.withUtkastId(eventId)
			.withIdent(ident)
			.delete()

		kafkaSender.publishUtkastNotification(eventId, utkast)

	}

	private fun publishBeskjedNotification(
		brukerNotifikasjonInfo: NotificationInfo,
		hendelsestidspunkt: LocalDateTime,
		key: String,
		eventId: String,
		notifikasjonsNokkel: NokkelInput
	) {
		val beskjedNotifikasjon = nyBeskjedNotifikasjon(
			brukerNotifikasjonInfo.notifikasjonsTittel,
			brukerNotifikasjonInfo.lenke,
			hendelsestidspunkt,
			brukerNotifikasjonInfo.antallAktiveDager,
			brukerNotifikasjonInfo.eksternVarsling
		)

		val notificationType = "Beskjed"
		loopAndPublishToKafka(key, notificationType) {
			logger.info(
				"$key: Varsel om $notificationType skal publiseres med eventId=$eventId og med lenke " +
					brukerNotifikasjonInfo.lenke
			)
			kafkaSender.publishBeskjedNotification(notifikasjonsNokkel, beskjedNotifikasjon)
		}
	}

	private fun publishDoneNotification(
		key: String,
		hendelsestidspunkt: LocalDateTime?,
		notifikasjonsNokkel: NokkelInput
	) {
		val doneNotifikasjon = DoneInputBuilder()
			.withTidspunkt(hendelsestidspunkt)
			.build()

		loopAndPublishToKafka(key, "Done") {
			kafkaSender.publishDoneNotification(notifikasjonsNokkel, doneNotifikasjon)
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

	private fun nyBeskjedNotifikasjon(
		title: String,
		lenke: String,
		hendelsestidspunkt: LocalDateTime,
		antallAktiveDager: Int,
		eksternVarsling: List<Varsel>
	): BeskjedInput {

		val synligFremTil = LocalDateTime.now().plusDays(antallAktiveDager.toLong())
		val builder = BeskjedInputBuilder()
			.withTekst(title)
			.withLink(URL(lenke))
			.withSikkerhetsnivaa(securityLevel)
			.withTidspunkt(hendelsestidspunkt)
			.withSynligFremTil(synligFremTil)
			.withEksternVarsling(eksternVarsling.isNotEmpty())

		for (varsel in eksternVarsling) {
			if (varsel.kanal == sms && varsel.tekst != null)
				builder.withSmsVarslingstekst(varsel.tekst)
			if (varsel.kanal == epost) {
				builder.withEpostVarslingstekst(varsel.tekst)
				builder.withEpostVarslingstittel(if (varsel.tittel == null || varsel.tittel?.length!! > 40)  defaultVarselTittel else varsel.tittel)
			}
		}


		return builder.build()
	}

	private fun nyOppgaveNotifikasjon(
		title: String,
		lenke: String,
		hendelsestidspunkt: LocalDateTime,
		antallAktiveDager: Int,
		eksternVarsling: List<Varsel>
	): OppgaveInput {

		val synligFremTil = LocalDateTime.now().plusDays(antallAktiveDager.toLong())
		val builder = OppgaveInputBuilder()
			.withTekst(title)
			.withLink(URL(lenke))
			.withSikkerhetsnivaa(securityLevel)
			.withTidspunkt(hendelsestidspunkt)
			.withSynligFremTil(synligFremTil)
			.withEksternVarsling(eksternVarsling.isNotEmpty())

		for (varsel in eksternVarsling) {
			if (varsel.kanal == epost) {
				builder.withEpostVarslingstekst(varsel.tekst)
				builder.withEpostVarslingstittel(varsel.tittel ?: defaultVarselTittel)
			}
			if (varsel.kanal == sms)
				builder.withSmsVarslingstekst(varsel.tekst)
		}

		return builder.build()
	}

	private fun createNotificationKey(eventId: String, fnr: String, groupId: String): NokkelInput {
		return NokkelInputBuilder()
			.withNamespace(notificationNamespace)
			.withAppnavn(appNavn)
			.withEventId(eventId)
			.withFodselsnummer(fnr)
			.withGrupperingsId(groupId)
			.build()
	}


	private fun isIdFromHenvendelse(id: String): Boolean {
		// behandlingsid opprettet av henvendelse består av 9 karakterer, prefix=10 [A-Z,a-z]
		// UUID eller ULID vil være 36 karakterer.
		return "10019To00".length == id.length
	}

	private fun createULIDEventId(henvendelsesId: String, oppgave: Boolean): String {
		val prefix = "00HENVEND1"
		val type = if (oppgave) "0P" else "BE"
		val henvendelsesIdFiltrert = henvendelsesId.substring(2).replace("""[ILOUilou]""".toRegex(), "0")
		val erstatningsStreng = henvendelsesId.substring(2)
			.toCharArray().map { erstattKarakter(it) }.joinToString("")

		return prefix + type + henvendelsesIdFiltrert + erstatningsStreng
	}


	private fun erstattKarakter(ch: Char): Char {
		return if ('I'.equals(ch, true)) '1'
		else if ('L'.equals(ch, true)) '2'
		else if ('O'.equals(ch, true)) '3'
		else if ('U'.equals(ch, true)) '4'
		else '0'
	}

	private fun correctNamespace(currentNamespace: String) = allowedNamespace.contains(currentNamespace)
}
