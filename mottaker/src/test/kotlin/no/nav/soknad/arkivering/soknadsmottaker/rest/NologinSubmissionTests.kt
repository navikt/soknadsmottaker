package no.nav.soknad.arkivering.soknadsmottaker.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import io.prometheus.metrics.model.registry.PrometheusRegistry
import no.nav.soknad.arkivering.avroschemas.InnsendingMetrics
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.KafkaException
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NologinSubmissionTests {

	@MockitoBean
	lateinit var prometheusRegistry: PrometheusRegistry

	@Autowired
	private lateinit var metrics: InnsendtMetrics

	@MockkBean(relaxed = true)
	private lateinit var kafkaSender: KafkaSender

	@Autowired
val nologinSubmission: NologinSubmission? = null

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

		nologinSubmission?.nologinSubmission(soknad, null)

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

