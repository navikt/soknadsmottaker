package no.nav.soknad.arkivering.soknadsmottaker.rest

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.prometheus.metrics.model.registry.PrometheusRegistry
import no.nav.soknad.arkivering.avroschemas.InnsendingMetrics
import no.nav.soknad.arkivering.avroschemas.Soknadarkivschema
import no.nav.soknad.arkivering.soknadsmottaker.config.KafkaConfig
import no.nav.soknad.arkivering.soknadsmottaker.service.ArchiverService
import no.nav.soknad.arkivering.soknadsmottaker.service.KafkaSender
import no.nav.soknad.arkivering.soknadsmottaker.service.MESSAGE_ID
import no.nav.soknad.arkivering.soknadsmottaker.service.convert
import no.nav.soknad.arkivering.soknadsmottaker.supervision.InnsendtMetrics
import no.nav.soknad.arkivering.soknadsmottaker.utils.createSoknad
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.kafka.KafkaException
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import java.util.concurrent.CompletableFuture

class ReceiverTests {

	private val topic = "privat-soknadinnsending-v1-dev"
	private val metricsTopic = "privat-soknadinnsending-metrics-v1-dev"

	private val kafkaMock = mockk<KafkaTemplate<String, Soknadarkivschema>>()
	private val metricsKafkaMock = mockk<KafkaTemplate<String, InnsendingMetrics>>()
	private val beskjedKafkaMock = mockk<KafkaTemplate<String, String>>()
	private val oppgaveKafkaMock = mockk<KafkaTemplate<String, String>>()
	private val doneKafkaMock = mockk<KafkaTemplate<String, String>>()
	private val utkastKafkaMock = mockk<KafkaTemplate<String, String>>()
	private val nologinSubmissionKafkaMock = mockk<KafkaTemplate<String, String>>()

	private val metrics = InnsendtMetrics(PrometheusRegistry.defaultRegistry)
	private val receiver = mockReceiver(metrics)

	@Test
	fun `When receiving REST call, message is put on Kafka`() {
		val errorsBefore = metrics.mottattErrorGet("BIL") ?: 0.0
		val sentInBefore = metrics.mottattSoknadGet("BIL") ?: 0.0
		val soknad = createSoknad()

		val record = slot<ProducerRecord<String, Soknadarkivschema>>()
		val metricRecord = slot<ProducerRecord<String, InnsendingMetrics>>()

		every { kafkaMock.send(capture(record)) } returns setFuture(makeSendResult(topic, convert(soknad)))
		every { metricsKafkaMock.send(capture(metricRecord)) } returns setFuture(
			makeSendResult(
				metricsTopic,
				InnsendingMetrics()
			)
		)

		receiver.receive(soknad, null)

		assertTrue(record.isCaptured)
		assertEquals(topic, record.captured.topic(), "Should send to the right topic")
		assertEquals(1, record.captured.headers().headers(MESSAGE_ID).count(), "Should have a MESSAGE_ID header")
		assertEquals("BIL", record.captured.value().arkivtema, "Should have correct tema")
		assertEquals(errorsBefore + 0.0, metrics.mottattErrorGet("BIL"), "Should not cause errors")
		assertEquals(sentInBefore + 1.0, metrics.mottattSoknadGet("BIL"), "Should increase counter by 1")

		assertEquals(metricsTopic, metricRecord.captured.topic(), "Should send metrics to the right topic")
		assertEquals(
			1, metricRecord.captured.headers().headers(MESSAGE_ID).count(),
			"Metrics should have a MESSAGE_ID header"
		)
		assertEquals(
			"soknadsmottaker", metricRecord.captured.value().application,
			"Metrics should have correct application name"
		)
		assertEquals("publish to kafka", metricRecord.captured.value().action, "Metrics should have correct action")
		assertTrue(
			metricRecord.captured.value().startTime <= System.currentTimeMillis(),
			"Metrics should have correct startTime"
		)
		assertTrue(
			metricRecord.captured.value().duration <= metricRecord.captured.value().startTime,
			"Metrics should have a duration"
		)

		metrics.unregister()
	}

	@Test
	fun `Exception is thrown if message is not put on Kafka`() {
		val soknad = createSoknad()
		val metricMessage = InnsendingMetrics()

		val record = slot<ProducerRecord<String, Soknadarkivschema>>()
		val metricRecord = slot<ProducerRecord<String, InnsendingMetrics>>()

		every { kafkaMock.send(capture(record)) } throws KafkaException("Mocked Exception")
		every { metricsKafkaMock.send(capture(metricRecord)) } returns setFuture(
			makeSendResult(
				metricsTopic,
				metricMessage
			)
		)

		assertThrows<KafkaException> {
			receiver.receive(soknad, null)
		}

		assertTrue(record.isCaptured)
		assertEquals(topic, record.captured.topic(), "Should send to the right topic")
		assertEquals(1, record.captured.headers().headers(MESSAGE_ID).count(), "Should have a MESSAGE_ID header")
		assertEquals("BIL", record.captured.value().arkivtema, "Should have correct tema")
		metrics.unregister()
	}

	private fun <T: Any> makeSendResult(topic: String, melding: T) = SendResult(
		ProducerRecord(topic, "123", melding),
		RecordMetadata(TopicPartition(topic, 1), 1L, 1, 1L, 1, 1)
	)

	private fun <T: Any> setFuture(v: SendResult<String, T>): CompletableFuture<SendResult<String, T>> {
		return CompletableFuture.completedFuture(v)
	}

	private fun mockReceiver(metrics: InnsendtMetrics): RestApi {
		val conf = KafkaConfig().also {
			it.namespace = "default"
			it.secure = "FALSE"
			it.schemaRegistryUsername = "user"
			it.schemaRegistryPassword = "pass"
			it.schemaRegistryUrl = "http://localhost:8081"
			it.kafkaBrokers = "localhost:29092"
			it.truststorePath = "path"
			it.keystorePath = "path"
			it.credstorePassword = "pass"

			it.mainTopic = topic
			it.metricsTopic = metricsTopic
			it.brukernotifikasjonDoneTopic = "min-side.aapen-brukernotifikasjon-done-v1"
			it.brukernotifikasjonBeskjedTopic = "min-side.aapen-brukernotifikasjon-beskjed-v1"
			it.brukernotifikasjonOppgaveTopic = "min-side.aapen-brukernotifikasjon-oppgave-v1"
			it.nologinSubmissionTopic = "privat-soknadinnsending-nologin-v1-dev"
		}
		val kafkaSender =
			KafkaSender(conf,
				kafkaMock,
				metricsKafkaMock,
				beskjedKafkaMock,
				oppgaveKafkaMock,
				doneKafkaMock,
				utkastKafkaMock,
				nologinKafkaTemplate = nologinSubmissionKafkaMock
			)
		val archiverService = ArchiverService(kafkaSender, metrics)
		return RestApi(archiverService)
	}
}
