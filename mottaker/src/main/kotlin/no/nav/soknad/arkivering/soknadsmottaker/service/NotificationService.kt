package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.brukernotifikasjon.schemas.builders.DoneInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.NokkelInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.OppgaveInputBuilder
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.brukernotifikasjon.schemas.input.OppgaveInput
import no.nav.soknad.arkivering.soknadsmottaker.config.KafkaConfig
import no.nav.soknad.arkivering.soknadsmottaker.model.NotificationInfo
import no.nav.soknad.arkivering.soknadsmottaker.model.SoknadRef
import no.nav.soknad.arkivering.soknadsmottaker.model.Varsel
import no.nav.soknad.arkivering.soknadsmottaker.model.Varsel.Kanal.epost
import no.nav.soknad.arkivering.soknadsmottaker.model.Varsel.Kanal.sms
import no.nav.tms.utkast.builder.UtkastJsonBuilder
import no.nav.tms.varsel.action.*
import no.nav.tms.varsel.builder.VarselActionBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
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
		publishBeskjedNotification(brukerNotifikasjonInfo = brukerNotifikasjonInfo, key = key, eventId = key, fodselsnummer = userId)
	}

	fun createNewNotification(key: String, soknad: SoknadRef, brukerNotifikasjonInfo: NotificationInfo) {
		if (!correctNamespace(kafkaConfig.namespace)) {
			logger.info("$key: Will not publish Beskjed/Oppgave, the namespace '${kafkaConfig.namespace}' is not correct.")
			return
		}

		logger.info("$key: skal opprette ny notifikasjon")

		//  Når en bruker tar initiativ til å opprette en søknad/ettersending lages det et utkast.
		//  Når systemet ser at det mangler påkrevde vedlegg som skal ettersendes, lages det en oppgave i stedet.
		if (soknad.erSystemGenerert == true) {
			publishOppgaveNotification(
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

	private fun publishOppgaveNotification(
		brukerNotifikasjonInfo: NotificationInfo,
		key: String,
		eventId: String,
		fodselsnummer: String
	) {

		val notification = createNewNotification(
			varseltype = Varseltype.Oppgave, eventId = eventId,
			persId = fodselsnummer, notificationInfo = brukerNotifikasjonInfo )

		loopAndPublishToKafka(key, Varseltype.Oppgave.name) {
			logger.info(
				"$key: Varsel om ${Varseltype.Oppgave} skal publiseres med eventId=$eventId og med lenke " +
					brukerNotifikasjonInfo.lenke
			)
			kafkaSender.publishOppgaveNotification(key = key, value = notification)
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

	private fun publishBeskjedNotification(
		brukerNotifikasjonInfo: NotificationInfo,
		key: String,
		eventId: String,
		fodselsnummer: String
	) {

		val notification = createNewNotification(
			varseltype = Varseltype.Beskjed, eventId = eventId,
			persId = fodselsnummer, notificationInfo = brukerNotifikasjonInfo )

		loopAndPublishToKafka(key, Varseltype.Beskjed.name) {
			logger.info(
				"$key: Varsel om ${Varseltype.Beskjed} skal publiseres med eventId=$eventId og med lenke " +
					brukerNotifikasjonInfo.lenke
			)
			kafkaSender.publishBeskjedNotification(key, notification)
		}
	}

	private fun publishDoneNotification(
		key: String,
	) {
		val doneNotification = deleteNotification(eventId = key)
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
/*

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
*/

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


	private fun erstattKarakter(ch: Char): Char {
		return if ('I'.equals(ch, true)) '1'
		else if ('L'.equals(ch, true)) '2'
		else if ('O'.equals(ch, true)) '3'
		else if ('U'.equals(ch, true)) '4'
		else '0'
	}

	private fun correctNamespace(currentNamespace: String) = allowedNamespace.contains(currentNamespace)

	private fun createNewNotification(
		varseltype: Varseltype?,
		eventId: String,
		persId: String,
		notificationInfo: NotificationInfo
	): String =
		VarselActionBuilder.opprett {
			type = varseltype
			varselId = eventId
			sensitivitet = Sensitivitet.High
			ident = persId
			tekster += Tekst(
				spraakkode = "nb",
				tekst = notificationInfo.notifikasjonsTittel,
				default = true
			)
			link = notificationInfo.lenke
			aktivFremTil = ZonedDateTime.now(ZoneId.of("Z")).plusDays(notificationInfo.antallAktiveDager.toLong())
			eksternVarsling = externalNotification(notificationInfo.eksternVarsling)
			//produsent = Produsent(cluster = if (activeProfile == "prod") "prod-gcp" else if (activeProfile == "dev") "dev-gcp" else "local", namespace = kafkaConfig.namespace, appnavn = "soknadsmottaker")
		}

	private fun deleteNotification(eventId: String): String =
		VarselActionBuilder.inaktiver { varselId = eventId }



	private fun externalNotification(eksternVarsling: List<Varsel>?): EksternVarslingBestilling? =
		if (eksternVarsling == null || eksternVarsling.isNotEmpty())
			null
		else
			EksternVarslingBestilling(
				prefererteKanaler = eksternVarsling.map{ if (it.kanal == Varsel.Kanal.sms) EksternKanal.SMS else EksternKanal.EPOST },
				smsVarslingstekst =  eksternVarsling.firstOrNull{it.kanal == Varsel.Kanal.sms}?.tekst,
				epostVarslingstittel = eksternVarsling.firstOrNull{it.kanal == Varsel.Kanal.epost}?.tittel,
				epostVarslingstekst = eksternVarsling.firstOrNull{it.kanal == Varsel.Kanal.epost}?.tekst
			)
	}
