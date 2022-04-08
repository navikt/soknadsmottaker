package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.brukernotifikasjon.schemas.builders.BeskjedInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.DoneInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.NokkelInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.OppgaveInputBuilder
import no.nav.brukernotifikasjon.schemas.input.BeskjedInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.brukernotifikasjon.schemas.input.OppgaveInput
import no.nav.soknad.arkivering.soknadsmottaker.config.AppConfiguration
import no.nav.soknad.arkivering.soknadsmottaker.model.NotificationInfo
import no.nav.soknad.arkivering.soknadsmottaker.model.SoknadRef
import no.nav.soknad.arkivering.soknadsmottaker.model.Varsel
import no.nav.soknad.arkivering.soknadsmottaker.model.Varsel.Kanal.epost
import no.nav.soknad.arkivering.soknadsmottaker.model.Varsel.Kanal.sms
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

@Service
class NotificationService(
	private val kafkaSender: KafkaSender,
	private val appConfiguration: AppConfiguration
) {

	private val logger = LoggerFactory.getLogger(javaClass)

	private val defaultVarselTittel = "Notifikasjon fra NAV"
	val appNavn = "soknadsmottaker"
	val allowedNamespace = listOf("team-soknad")
	val notificationNamespace = "min-side."

	private val securityLevel = 4 // Forutsetter at brukere har logget seg på f.eks. bankId slik at nivå 4 er oppnådd


	fun newNotification(key: String, soknad: SoknadRef, brukerNotifikasjonInfo: NotificationInfo) {
		if (!correctNamespace(appConfiguration.kafkaConfig.namespace)) {
			logger.info("$key: Will not publish Beskjed/Oppgave, the namespace '${appConfiguration.kafkaConfig.namespace}' " +
				"is not correct.")
			return
		}

		val eventId = if (isIdFromHenvendelse(key)) createULIDEventId(key, soknad.erEttersendelse) else	key
		val notifikasjonsNokkel = createNotificationKey(eventId, soknad.personId, soknad.groupId)

		val hendelsestidspunkt = soknad.tidpunktEndret.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()

		if (!soknad.erEttersendelse) {
			val beskjedNotifikasjon = nyBeskjedNotifikasjon(
				brukerNotifikasjonInfo.notifikasjonsTittel,
				brukerNotifikasjonInfo.lenke,
				hendelsestidspunkt,
				brukerNotifikasjonInfo.antallAktiveDager,
				brukerNotifikasjonInfo.eksternVarsling
			)

			logger.info("$eventId: Varsel om Beskjed for $key med lenke ${brukerNotifikasjonInfo.lenke} skal publiseres")
			kafkaSender.publishBeskjedNotification(notifikasjonsNokkel, beskjedNotifikasjon)

		} else {
			val oppgaveNotifikasjon = nyOppgaveNotifikasjon(
				brukerNotifikasjonInfo.notifikasjonsTittel,
				brukerNotifikasjonInfo.lenke,
				hendelsestidspunkt,
				brukerNotifikasjonInfo.antallAktiveDager,
				brukerNotifikasjonInfo.eksternVarsling
			)
			logger.info("$eventId: Varsel om Oppgave for $key med lenke ${brukerNotifikasjonInfo.lenke} skal publiseres")
			kafkaSender.publishOppgaveNotification(notifikasjonsNokkel, oppgaveNotifikasjon)
		}
	}

	fun cancelNotification(key: String, soknad: SoknadRef) {
		if (!correctNamespace(appConfiguration.kafkaConfig.namespace)) {
			logger.info("$key: Will not publish Done Event, the namespace '${appConfiguration.kafkaConfig.namespace}' " +
				"is not correct.")
			return
		}

		val eventId = if (isIdFromHenvendelse(key)) createULIDEventId(key, soknad.erEttersendelse) else	key
		val notifikasjonsNokkel = createNotificationKey(eventId, soknad.personId, soknad.groupId)
		val hendelsestidspunkt = soknad.tidpunktEndret.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()

		val doneNotifikasjon = DoneInputBuilder()
			.withTidspunkt(hendelsestidspunkt)
			.build()

		kafkaSender.publishDoneNotification(notifikasjonsNokkel, doneNotifikasjon)
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
			if (varsel.kanal == sms)
				builder.withSmsVarslingstekst(varsel.tekst)
			if (varsel.kanal == epost) {
				builder.withEpostVarslingstekst(varsel.tekst)
				builder.withEpostVarslingstittel(varsel.tittel ?: defaultVarselTittel)
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

	private fun createNotificationKey(enventId: String, fnr: String, groupId: String): NokkelInput {
		return NokkelInputBuilder()
			.withNamespace(notificationNamespace)
			.withAppnavn(appNavn)
			.withEventId(enventId)
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
