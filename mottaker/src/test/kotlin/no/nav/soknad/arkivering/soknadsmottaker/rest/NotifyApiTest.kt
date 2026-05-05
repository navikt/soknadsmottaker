package no.nav.soknad.arkivering.soknadsmottaker.rest

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import io.prometheus.metrics.model.registry.PrometheusRegistry
import no.nav.soknad.arkivering.soknadsmottaker.SoknadsmottakerApplication
import no.nav.soknad.arkivering.soknadsmottaker.config.KafkaConfig
import no.nav.soknad.arkivering.soknadsmottaker.model.AddNotification
import no.nav.soknad.arkivering.soknadsmottaker.model.NotificationInfo
import no.nav.soknad.arkivering.soknadsmottaker.model.SoknadRef
import no.nav.soknad.arkivering.soknadsmottaker.service.KafkaSender
import no.nav.soknad.arkivering.soknadsmottaker.supervision.InnsendtMetrics
import no.nav.tms.varsel.builder.BuilderEnvironment
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.springframework.http.HttpStatus
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID


@ActiveProfiles("test")
@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = ["spring.main.allow-bean-definition-overriding=true"],
	classes = [SoknadsmottakerApplication::class]
)
@ExtendWith(
	SpringExtension::class
)
@AutoConfigureWebTestClient

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotifyApiTest {
	@MockitoBean
	lateinit var prometheusRegistry: PrometheusRegistry

	@MockitoSpyBean
	private lateinit var metrics: InnsendtMetrics

	@Autowired
	private lateinit var kafkaConfig: KafkaConfig

	@MockkBean
	private lateinit var kafkaSender: KafkaSender

	@Autowired
	private lateinit var notifyApi: NotifyApiImpl

	@Autowired
	private lateinit var objectMapper: ObjectMapper


	@Test
	fun `publishes new user started application notification to Utkast topic`() {
		// Given
		val innsendingsId = UUID.randomUUID().toString()
		val soknadRef = SoknadRef(
			innsendingId = innsendingsId,
			erEttersendelse = false,
			groupId = innsendingsId,
			personId = "01234567891",
			tidpunktEndret = OffsetDateTime.of(2026, 5, 1, 12, 0, 0, 0, ZoneOffset.ofHours(2)),
			erSystemGenerert = false
		)
		val brukerNotificationInfo = NotificationInfo(
			notifikasjonsTittel = "Utkast til søknad om tilleggsstøtte",
			lenke = "https://fyllut-preprod.intern.dev.nav.no/fyllut/nav060404/oppsummering?sub=digital&innsendingsId=e2a53194-49c3-4146-a868-dd9dcff3d0ca",
			antallAktiveDager = 1,
			eksternVarsling = emptyList(),
			utsettSendingTil = null
		)
		val notification = AddNotification(
			soknadRef = soknadRef,
			brukernotifikasjonInfo = brukerNotificationInfo
		)

		val msgKey = slot<String>()
		val notificationString = slot<String>()

		val now = OffsetDateTime.now()
		val slettesEtter = now
			.toLocalDate()
			.plusDays(brukerNotificationInfo.antallAktiveDager.toLong())
			.atTime(2, 0)
			.atOffset(now.offset)

		every { kafkaSender.publishUtkastNotification(capture(msgKey), capture(notificationString)) } returns Unit

		// When
		val response = notifyApi.newNotification(notification, innsendingsId )

		// Then
		assertTrue( response.statusCode == HttpStatus.OK )

		assertTrue(notificationString.isCaptured, "Should capture new notification message")
		val utkastMsg = notificationString.captured

		assertTrue (utkastMsg.contains(innsendingsId))
		assertTrue (utkastMsg.contains("\"utkastId\":\"$innsendingsId\""))
		assertTrue (utkastMsg.contains("\"ident\":\"${soknadRef.personId}\""))
		assertTrue (utkastMsg.contains("\"slettesEtter\":\"${slettesEtter.toString()}\""))

	}

	@Test
	fun `publishes new system started application notification to Oppgave topic`() {
		// Given
		val innsendingsId = UUID.randomUUID().toString()
		val soknadRef = SoknadRef(
			innsendingId = innsendingsId,
			erEttersendelse = true,
			groupId = innsendingsId,
			personId = "01234567891",
			tidpunktEndret = OffsetDateTime.of(2026, 5, 1, 12, 0, 0, 0, ZoneOffset.ofHours(2)),
			erSystemGenerert = true
		)
		val brukerNotificationInfo = NotificationInfo(
			notifikasjonsTittel = "Ettersending til søknad om tilleggsstøtte",
			lenke = "https://fyllut-preprod.intern.dev.nav.no/fyllut/nav060404/oppsummering?sub=digital&innsendingsId=e2a53194-49c3-4146-a868-dd9dcff3d0ca",
			antallAktiveDager = 14,
			eksternVarsling = emptyList(),
			utsettSendingTil = null
		)
		val notification = AddNotification(
			soknadRef = soknadRef,
			brukernotifikasjonInfo = brukerNotificationInfo
		)

		val msgKey = slot<String>()
		val notificationString = slot<String>()

		val now = OffsetDateTime.now()
		val slettesEtter = now
			.toLocalDate()
			.plusDays(brukerNotificationInfo.antallAktiveDager.toLong())
			.atTime(2, 0)
			.atOffset(now.offset)
		val slettesEtterString = objectMapper.writeValueAsString(slettesEtter).replace("\"", "")

		every { kafkaSender.publishOppgaveNotification(capture(msgKey), capture(notificationString)) } returns Unit

		// When
		val response = notifyApi.newNotification(notification, innsendingsId )

		// Then
		assertTrue( response.statusCode == HttpStatus.OK )

		assertTrue(notificationString.isCaptured, "Should capture new notification message")
		val oppgaveMsg = notificationString.captured

		assertTrue (oppgaveMsg.contains(innsendingsId))
		assertTrue (oppgaveMsg.contains("\"type\":\"oppgave\""))
		assertTrue (oppgaveMsg.contains("\"varselId\":\"$innsendingsId\""))
		assertTrue (oppgaveMsg.contains("\"ident\":\"${soknadRef.personId}\""))
		assertTrue (oppgaveMsg.contains("\"aktivFremTil\":\"$slettesEtterString\""), "Uventet aktivFremtil verdi")

	}


	@Test
	fun `publishes done message when cancelNotification is called`() {
		// Given
		val innsendingsId = UUID.randomUUID().toString()
		val soknadRef = SoknadRef(
			innsendingId = innsendingsId,
			erEttersendelse = true,
			groupId = innsendingsId,
			personId = "01234567891",
			tidpunktEndret = OffsetDateTime.of(2026, 5, 1, 12, 0, 0, 0, ZoneOffset.ofHours(2)),
			erSystemGenerert = true
		)

		val msgKey = slot<String>()
		val notificationString = slot<String>()
		val msgKeyUtkast = slot<String>()
		val notificationStringUtkast = slot<String>()

		val now = OffsetDateTime.now()

		every { kafkaSender.publishDoneNotification(capture(msgKey), capture(notificationString)) } returns Unit
		every { kafkaSender.publishUtkastNotification(capture(msgKeyUtkast), capture(notificationStringUtkast)) } returns Unit

		// When
		val response = notifyApi.cancelNotification(soknadRef, innsendingsId )

		// Then
		assertTrue( response.statusCode == HttpStatus.OK )

		assertEquals(innsendingsId, msgKey.captured, "Should publish with correct key")
		assertEquals(innsendingsId, msgKeyUtkast.captured, "Should)	 publish utkast done notification with correct key")
		assertTrue(notificationString.isCaptured, "Should capture done notification message")
		assertTrue(notificationStringUtkast.isCaptured, "Should capture done utkast notification message")
		val oppgaveDoneMsg = notificationString.captured
		val utkastDoneMsg = notificationStringUtkast.captured

		assertTrue (oppgaveDoneMsg.contains("\"varselId\":\"$innsendingsId\""))
		assertTrue (utkastDoneMsg.contains("\"utkastId\":\"$innsendingsId\""))

	}


}

