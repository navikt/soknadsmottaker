package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.brukernotifikasjon.schemas.builders.BeskjedInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.DoneInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.NokkelInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.OppgaveInputBuilder
import no.nav.brukernotifikasjon.schemas.input.BeskjedInput
import no.nav.brukernotifikasjon.schemas.input.DoneInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.brukernotifikasjon.schemas.input.OppgaveInput
import no.nav.soknad.arkivering.soknadsmottaker.model.NotificationInfo
import no.nav.soknad.arkivering.soknadsmottaker.model.SoknadRef
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeaders
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit


@Service
class NotificationService(
	private val kafkaBeskjedTemplate: KafkaTemplate<NokkelInput, BeskjedInput>,
	private val kafkaOppgaveTemplate: KafkaTemplate<NokkelInput, OppgaveInput>,
	private val kafkaDoneTemplate: KafkaTemplate<NokkelInput, DoneInput>
) {

	private val logger = LoggerFactory.getLogger(javaClass)

	val appNavn = "soknadsmottaker"
	val nameSpace = "min-side."

	private val securityLevel = 4 // Forutsetter at brukere har logget seg på f.eks. bankId slik at nivå 4 er oppnådd
	private var levetidOpprettetSoknad: Long = (System.getProperty("publiserSoknadEndring.levetid.opprettetsoknad") ?: "56").toLong()
	private var levetidEttersending: Long = (System.getProperty("publiserSoknadEndring.levetid.ettersending") ?: "14").toLong()


	private fun <T> publish(topic: String, key: NokkelInput, value: T, kafkaTemplate: KafkaTemplate<NokkelInput, T>) {
		val producerRecord = ProducerRecord(nameSpace+topic, key, value)
		val headers = RecordHeaders()
		headers.add(MESSAGE_ID, UUID.randomUUID().toString().toByteArray())
		headers.forEach { h -> producerRecord.headers().add(h) }

		val future = kafkaTemplate.send(producerRecord)
		future.get(10, TimeUnit.SECONDS)
		logger.info("$key: Published to $topic")
	}


	fun newNotification(key: String, soknad: SoknadRef, brukerNotifikasjonInfo: NotificationInfo) {
		val eventId = if (isIdFromHenvendelse(key)) createULIDEventId(key, soknad.erEttersendelse) else	key
		val notifikasjonsNokkel = createNotificationKey(eventId, soknad.personId, soknad.groupId)

		val hendelsestidspunkt = toLocalDate(soknad.endringsDato)

		if (!soknad.erEttersendelse) {
			val beskjedNotifikasjon = nyBeskjedNotifikasjon(
				brukerNotifikasjonInfo.notifikasjonsTittel,
				brukerNotifikasjonInfo.lenke,
				hendelsestidspunkt
			)
			logger.info("$eventId: Varsel om Beskjed for $key med lenke ${brukerNotifikasjonInfo.lenke} skal publiseres")
			publish("aapen-brukernotifikasjon-beskjed-v1", notifikasjonsNokkel, beskjedNotifikasjon, kafkaBeskjedTemplate)

		} else {
			val oppgaveNotifikasjon = nyOppgaveNotifikasjon(
				brukerNotifikasjonInfo.notifikasjonsTittel,
				brukerNotifikasjonInfo.lenke,
				hendelsestidspunkt
			)
			logger.info("$eventId: Varsel om Oppgave for $key med lenke ${brukerNotifikasjonInfo.lenke} skal publiseres")
			publish("aapen-brukernotifikasjon-oppgave-v1", notifikasjonsNokkel, oppgaveNotifikasjon, kafkaOppgaveTemplate)
		}

	}

	fun cancelNotification(key: String, soknad: SoknadRef) {
		val eventId = if (isIdFromHenvendelse(key)) createULIDEventId(key, soknad.erEttersendelse) else	key
		val notifikasjonsNokkel = createNotificationKey(eventId, soknad.personId, soknad.groupId)
		val hendelsestidspunkt = toLocalDate(soknad.endringsDato)

		val doneNotifikasjon = DoneInputBuilder()
			.withTidspunkt(hendelsestidspunkt)
			.build()

		publish("aapen-brukernotifikasjon-done-v1", notifikasjonsNokkel, doneNotifikasjon, kafkaDoneTemplate)
	}

	private fun nyBeskjedNotifikasjon(title: String, lenke: String, hendelsestidspunkt: LocalDateTime): BeskjedInput {
		val dagerSidenhendelsen = hendelsestidspunkt.until(LocalDateTime.now(), ChronoUnit.DAYS)
		val synligFremTil = LocalDateTime.now().plusDays(levetidOpprettetSoknad-dagerSidenhendelsen)
		return BeskjedInputBuilder()
			.withTekst(title)
			.withLink(URL(lenke))
			.withSikkerhetsnivaa(securityLevel)
			.withTidspunkt(hendelsestidspunkt)
			.withSynligFremTil(synligFremTil)
			.withEksternVarsling(false)
			.build()
	}

	private fun nyOppgaveNotifikasjon(title: String, lenke: String, hendelsestidspunkt: LocalDateTime): OppgaveInput {
		val dagerSidenhendelsen = hendelsestidspunkt.until(LocalDateTime.now(), ChronoUnit.DAYS)
		val synligFremTil = LocalDateTime.now().plusDays(levetidEttersending-dagerSidenhendelsen)
		return OppgaveInputBuilder()
			.withTekst(title)
			.withLink(URL(lenke))
			.withSikkerhetsnivaa(securityLevel)
			.withTidspunkt(hendelsestidspunkt)
			.withSynligFremTil(synligFremTil)
			.withEksternVarsling(false)
			.build()
	}

	private fun toLocalDate(dateString: String): LocalDateTime {
		val formatter = DateTimeFormatter.ISO_DATE_TIME
		return LocalDateTime.parse(dateString, formatter)
	}

	private fun createNotificationKey(enventId: String, fnr: String, groupId: String): NokkelInput {
		return NokkelInputBuilder()
			.withNamespace(nameSpace)
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
			.toCharArray().asList().map { erstattKarakter(it) }.joinToString("")

		return prefix + type + henvendelsesIdFiltrert + erstatningsStreng
	}


	private fun erstattKarakter(ch: Char): Char {
		if ('I'.equals(ch, true)) return '1'
		else if ('L'.equals(ch, true)) return '2'
		else if ('O'.equals(ch, true)) return '3'
		else if ('U'.equals(ch, true)) return '4'
		else return '0'
	}

}
