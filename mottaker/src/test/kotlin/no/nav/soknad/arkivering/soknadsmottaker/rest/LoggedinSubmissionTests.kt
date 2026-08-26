package no.nav.soknad.arkivering.soknadsmottaker.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import io.prometheus.metrics.model.registry.PrometheusRegistry
import no.nav.soknad.arkivering.soknadsmottaker.SoknadsmottakerApplication
import no.nav.soknad.arkivering.soknadsmottaker.config.KafkaConfig
import no.nav.soknad.arkivering.soknadsmottaker.model.AvsenderDto
import no.nav.soknad.arkivering.soknadsmottaker.model.BrukerDto
import no.nav.soknad.arkivering.soknadsmottaker.model.InnsendingMetrics
import no.nav.soknad.arkivering.soknadsmottaker.model.InnsendingTopicMsg
import no.nav.soknad.arkivering.soknadsmottaker.service.KafkaSender
import no.nav.soknad.arkivering.soknadsmottaker.supervision.InnsendtMetrics
import no.nav.soknad.arkivering.soknadsmottaker.supervision.MetricNames
import no.nav.soknad.arkivering.soknadsmottaker.utils.createInnsending
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.kafka.KafkaException
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.context.junit.jupiter.SpringExtension


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
class LoggedinSubmissionTests {

	@MockitoBean
	lateinit var prometheusRegistry: PrometheusRegistry

	@MockitoSpyBean
	private lateinit var metrics: InnsendtMetrics

	@Autowired
	private lateinit var kafkaConfig: KafkaConfig

	@MockkBean
	private lateinit var kafkaSender: KafkaSender

	@Autowired
	private lateinit var loggedInSubmission: LoggedInSubmission

	@Autowired
	private lateinit var objectMapper: ObjectMapper

	@Test
	fun `When receiving call on loggedin Rest endpoint, message is put on Kafka`() {

		// Given
		val errorsBefore = metrics.mottattSoknadGet(MetricNames.INNSENDT_ERROR.name, "HJE") ?: 0.0
		val sentInBefore = metrics.mottattSoknadGet(MetricNames.INNSENDT_OK.name,"HJE") ?: 0.0
		val brukerId = "01234567891"
		val avsenderId = "12345678901"
		val soknad = createInnsending(
			brukerDto = BrukerDto(brukerId, BrukerDto.IdType.FNR),
			avsenderDto = AvsenderDto(
				id = avsenderId,
				idType = AvsenderDto.IdType.FNR,
				navn = null
			), kanal = "NAV_NO", tema = "HJE"
		)

		val msgKey = slot<String>()
		val innsendingMsg = slot<String>()
		val isLoggedIn = slot<Boolean>()
		val metricKey = slot<String>()
		val metricMsg = slot<InnsendingMetrics>()

		every { kafkaSender.publishSubmission(capture(msgKey), capture(innsendingMsg), capture(isLoggedIn)) } returns Unit
		every { kafkaSender.publishMetric(capture(metricKey), capture(metricMsg)) } returns Unit

		// When
		loggedInSubmission.loggedInSubmission(soknad, null)

		// Expect
		assertTrue(msgKey.isCaptured, "Should capture message key")
		assertEquals(soknad.innsendingsId, msgKey.captured, "Should use innsendingsId as message key")
		assertTrue(isLoggedIn.isCaptured)
		assertEquals(true, isLoggedIn.captured, "Should be logged in")
		assertTrue(innsendingMsg.isCaptured, "Should capture innsending message")
		val submission = objectMapper.readValue(innsendingMsg.captured, InnsendingTopicMsg::class.java)
		assertEquals(soknad.tema, submission.arkivtema, "Should send correct tema")
		assertEquals(soknad.kanal, submission.kanal, "Should send correct kanal")
		assertEquals(soknad.brukerDto?.id, submission.brukerDto?.id, "Should send correct brukerId")
		assertEquals(soknad.avsenderDto.id, submission.avsenderDto.id, "Should send correct avsenderId")

		assertTrue(metricKey.isCaptured, "Should capture metric key")
		assertEquals(soknad.innsendingsId, metricKey.captured, "Should use innsendingsId as metric key")
		assertTrue(metricMsg.isCaptured, "Should capture metric message")
		assertEquals("soknadsmottaker", metricMsg.captured.application, "Metrics should have correct application name")
		assertEquals("publish to kafka", metricMsg.captured.action, "Metrics should have correct action")
		assertEquals(0, metricMsg.captured.startTime.offset.totalSeconds, "Metrics startTime should be UTC")
		assertTrue(metricMsg.captured.duration >= 0, "Metrics duration should be milliseconds")

		assertEquals(errorsBefore + 0.0, metrics.mottattSoknadGet(MetricNames.INNSENDT_ERROR.name, "HJE"), "Should not cause errors")
		assertEquals(sentInBefore + 1.0, metrics.mottattSoknadGet(MetricNames.INNSENDT_OK.name,"HJE"), "Should increase counter by 1")

	}

	@Test
	fun `When receiving call on loggedin Rest endpoint with empty brukerDto test input mapping`() {

		// Given
		val errorsBefore = metrics.mottattSoknadGet(MetricNames.INNSENDT_ERROR.name, "HJE") ?: 0.0
		val sentInBefore = metrics.mottattSoknadGet(MetricNames.INNSENDT_OK.name,"HJE") ?: 0.0
		val avsenderId = "123456789"
		val soknad = createInnsending(
			brukerDto = null,
			avsenderDto = AvsenderDto(
				id = avsenderId,
				idType = AvsenderDto.IdType.ORGNR,
				navn = null
			),
			kanal = "NAV_NO",
			tema = "HJE"
		)

		val msgKey = slot<String>()
		val innsendingMsg = slot<String>()
		val isLoggedIn = slot<Boolean>()
		val metricKey = slot<String>()
		val metricMsg = slot<InnsendingMetrics>()

		every { kafkaSender.publishSubmission(capture(msgKey), capture(innsendingMsg), capture(isLoggedIn)) } returns Unit
		every { kafkaSender.publishMetric(capture(metricKey), capture(metricMsg)) } returns Unit

		// When
		loggedInSubmission.loggedInSubmission(soknad, null)

		// Expect
		assertTrue(msgKey.isCaptured, "Should capture message key")
		assertEquals(soknad.innsendingsId, msgKey.captured, "Should use innsendingsId as message key")
		assertTrue(isLoggedIn.isCaptured)
		assertEquals(true, isLoggedIn.captured, "Should be logged in")
		assertTrue(innsendingMsg.isCaptured, "Should capture innsending message")
		val submission = objectMapper.readValue(innsendingMsg.captured, InnsendingTopicMsg::class.java)
		assertEquals(soknad.tema, submission.arkivtema, "Should send correct tema")
		assertEquals(soknad.kanal, submission.kanal, "Should send correct kanal")
		assertEquals(soknad.brukerDto, submission.brukerDto, "Should send correct brukerId")
		assertEquals(soknad.avsenderDto.id, submission.avsenderDto.id, "Should send correct avsenderId")
		assertEquals(soknad.avsenderDto.idType, submission.avsenderDto.idType, "Should send correct avsenderType")
		assertEquals(soknad.dokumenter.size, submission.dokumenter.size, "Should send correct number of documents")
		assertEquals(soknad.dokumenter.filter{it.erHovedskjema}.first().varianter.map{it.variantFormat},
			submission.dokumenter.filter{it.erHovedskjema}.first().varianter.map{it.variantFormat}, "Should send correct variantFormats")
		assertEquals(soknad.dokumenter.map{it.varianter.map{variant-> variant.uuid}}.flatten(),
			submission.dokumenter.map{it.varianter.map{variant-> variant.uuid}}.flatten(), "Should send correct uuids")

		assertTrue(metricKey.isCaptured, "Should capture metric key")
		assertEquals(soknad.innsendingsId, metricKey.captured, "Should use innsendingsId as metric key")
		assertTrue(metricMsg.isCaptured, "Should capture metric message")
		assertEquals("soknadsmottaker", metricMsg.captured.application, "Metrics should have correct application name")
		assertEquals("publish to kafka", metricMsg.captured.action, "Metrics should have correct action")
		assertEquals(0, metricMsg.captured.startTime.offset.totalSeconds, "Metrics startTime should be UTC")
		assertTrue(metricMsg.captured.duration >= 0, "Metrics duration should be milliseconds")

		assertEquals(errorsBefore + 0.0, metrics.mottattSoknadGet(MetricNames.INNSENDT_ERROR.name, "HJE"), "Should not cause errors")
		assertEquals(sentInBefore + 1.0, metrics.mottattSoknadGet(MetricNames.INNSENDT_OK.name,"HJE"), "Should increase counter by 1")

	}

	@Test
	fun `Exception is thrown if message is not put on Kafka`() {
		// Given
		val soknad = createInnsending(
			brukerDto = BrukerDto("01234567891", BrukerDto.IdType.FNR),
			avsenderDto = AvsenderDto(
				id = "01234567891",
				idType = AvsenderDto.IdType.FNR,
				navn = null
			), kanal = "NAV_NO", tema = "HJE"
		)
		val errorsBefore = metrics.mottattSoknadGet(MetricNames.INNSENDT_ERROR.name, "HJE") ?: 0.0
		val sentInBefore = metrics.mottattSoknadGet(MetricNames.INNSENDT_OK.name,"HJE")	?: 0.0

		val msgKey = slot<String>()
		val innsendingMsg = slot<String>()
		val isLoggedIn = slot<Boolean>()
		val metricKey = slot<String>()
		val metricMsg = slot<InnsendingMetrics>()

		every { kafkaSender.publishSubmission(capture(msgKey), capture(innsendingMsg), capture(isLoggedIn)) }  throws KafkaException("Mocked Exception")
		every { kafkaSender.publishMetric(capture(metricKey), capture(metricMsg)) } returns Unit

		// When
		assertThrows<KafkaException> {
			loggedInSubmission.loggedInSubmission(soknad, null)
		}

		// Expect
		assertTrue(msgKey.isCaptured, "Should capture message key")
		assertEquals(soknad.innsendingsId, msgKey.captured, "Should use innsendingsId as message key")
		assertEquals(true, isLoggedIn.captured, "Should be logged in")
		assertTrue(innsendingMsg.isCaptured, "Should capture innsending message")
		val submission = objectMapper.readValue(innsendingMsg.captured, InnsendingTopicMsg::class.java)
		assertEquals(soknad.tema, submission.arkivtema, "Should send correct tema")
		assertEquals(soknad.kanal, submission.kanal, "Should send correct kanal")
		assertEquals(soknad.brukerDto?.id, submission.brukerDto?.id, "Should send correct brukerId")

		assertTrue(metricKey.isCaptured, "Should capture metric key")
		assertEquals(soknad.innsendingsId, metricKey.captured, "Should use innsendingsId as metric key")
		assertTrue(metricMsg.isCaptured, "Should capture metric message")
		assertEquals("soknadsmottaker", metricMsg.captured.application, "Metrics should have correct application name")
		assertEquals("publish to kafka", metricMsg.captured.action, "Metrics should have correct action")
		assertEquals(0, metricMsg.captured.startTime.offset.totalSeconds, "Metrics startTime should be UTC")
		assertTrue(metricMsg.captured.duration >= 0, "Metrics duration should be milliseconds")

		assertEquals(errorsBefore + 1.0, metrics.mottattSoknadGet(MetricNames.INNSENDT_ERROR.name, "HJE"), "Should not cause errors")
		assertEquals(sentInBefore + 0.0, metrics.mottattSoknadGet(MetricNames.INNSENDT_OK.name,"HJE"), "Should increase counter by 1")

	}


}
