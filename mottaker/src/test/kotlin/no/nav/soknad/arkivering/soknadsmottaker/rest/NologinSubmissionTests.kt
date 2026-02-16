package no.nav.soknad.arkivering.soknadsmottaker.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import io.prometheus.metrics.model.registry.PrometheusRegistry
import no.nav.soknad.arkivering.avroschemas.InnsendingMetrics
import no.nav.soknad.arkivering.soknadsmottaker.SoknadsmottakerApplication
import no.nav.soknad.arkivering.soknadsmottaker.model.BrukerDto
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
class NologinSubmissionTests {

	@MockitoBean
	lateinit var prometheusRegistry: PrometheusRegistry

	@MockitoSpyBean
	private lateinit var metrics: InnsendtMetrics

	@MockkBean(relaxed = true)
	private lateinit var kafkaSender: KafkaSender

	@Autowired
	private lateinit var nologinSubmission: NologinSubmission


	@Test
	fun `When receiving call on nologin Rest endpoint, message is put on Kafka`() {
		val errorsBefore = metrics.mottattSoknadGet(MetricNames.INNSENDT_UINNLOGGET_ERROR.name, "HJE") ?: 0.0
		val sentInBefore = metrics.mottattSoknadGet(MetricNames.INNSENDT_UINNLOGGET.name,"HJE") ?: 0.0
		val soknad = createInnsending(
			brukerDto = BrukerDto("01234567891", BrukerDto.IdType.FNR),
		)

		val msgKey = slot<String>()
		val innsendingMsg = slot<String>()
		val metricKey = slot<String>()
		val metricMsg = slot<InnsendingMetrics>()

		every { kafkaSender.publishNologinSubmission(capture(msgKey), capture(innsendingMsg)) } returns Unit
		every { kafkaSender.publishMetric(capture(metricKey), capture(metricMsg)) } returns Unit

		nologinSubmission.nologinSubmission(soknad, null)

		assertTrue(msgKey.isCaptured, "Should capture message key")
		assertEquals(soknad.innsendingsId, msgKey.captured, "Should use innsendingsId as message key")
		assertTrue(innsendingMsg.isCaptured, "Should capture innsending message")
		assertTrue(innsendingMsg.captured.contains("HJE"), "Should contain tema = HJE")

		assertTrue(metricKey.isCaptured, "Should capture metric key")
		assertEquals(soknad.innsendingsId, metricKey.captured, "Should use innsendingsId as metric key")
		assertTrue(metricMsg.isCaptured, "Should capture metric message")
		assertEquals("soknadsmottaker", metricMsg.captured.application, "Metrics should have correct application name")

		assertEquals(errorsBefore + 0.0, metrics.mottattSoknadGet(MetricNames.INNSENDT_UINNLOGGET_ERROR.name, "HJE"), "Should not cause errors")
		assertEquals(sentInBefore + 1.0, metrics.mottattSoknadGet(MetricNames.INNSENDT_UINNLOGGET.name,"HJE"), "Should increase counter by 1")

	}

	@Test
	fun `Exception is thrown if message is not put on Kafka`() {
		val soknad = createInnsending(
			brukerDto = BrukerDto("01234567891", BrukerDto.IdType.FNR),
		)
		val errorsBefore = metrics.mottattSoknadGet(MetricNames.INNSENDT_UINNLOGGET_ERROR.name, "HJE") ?: 0.0
		val sentInBefore = metrics.mottattSoknadGet(MetricNames.INNSENDT_UINNLOGGET.name,"HJE")	?: 0.0

		val msgKey = slot<String>()
		val innsendingMsg = slot<String>()
		val metricKey = slot<String>()
		val metricMsg = slot<InnsendingMetrics>()

		every { kafkaSender.publishNologinSubmission(capture(msgKey), capture(innsendingMsg)) }  throws KafkaException("Mocked Exception")
		every { kafkaSender.publishMetric(capture(metricKey), capture(metricMsg)) } returns Unit

		assertThrows<KafkaException> {
			nologinSubmission?.nologinSubmission(soknad, null)
		}

		assertTrue(msgKey.isCaptured, "Should capture message key")
		assertEquals(soknad.innsendingsId, msgKey.captured, "Should use innsendingsId as message key")
		assertTrue(innsendingMsg.isCaptured, "Should capture innsending message")
		assertTrue(innsendingMsg.captured.contains("HJE"), "Should contain tema = HJE")

		assertTrue(metricKey.isCaptured, "Should capture metric key")
		assertEquals(soknad.innsendingsId, metricKey.captured, "Should use innsendingsId as metric key")
		assertTrue(metricMsg.isCaptured, "Should capture metric message")
		assertEquals("soknadsmottaker", metricMsg.captured.application, "Metrics should have correct application name")

		assertEquals(errorsBefore + 1.0, metrics.mottattSoknadGet(MetricNames.INNSENDT_UINNLOGGET_ERROR.name, "HJE"), "Should not cause errors")
		assertEquals(sentInBefore + 0.0, metrics.mottattSoknadGet(MetricNames.INNSENDT_UINNLOGGET.name,"HJE"), "Should increase counter by 1")

	}

}

