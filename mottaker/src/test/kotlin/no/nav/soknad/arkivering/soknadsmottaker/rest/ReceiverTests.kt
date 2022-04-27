package no.nav.soknad.arkivering.soknadsmottaker.rest

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.prometheus.client.CollectorRegistry
import no.nav.brukernotifikasjon.schemas.input.BeskjedInput
import no.nav.brukernotifikasjon.schemas.input.DoneInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.brukernotifikasjon.schemas.input.OppgaveInput
import no.nav.soknad.arkivering.avroschemas.InnsendingMetrics
import no.nav.soknad.arkivering.avroschemas.Soknadarkivschema
import no.nav.soknad.arkivering.soknadsmottaker.config.AppConfiguration
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
import org.springframework.util.concurrent.SettableListenableFuture

class ReceiverTests {

	private val topic = "privat-soknadInnsendt-v1-teamsoknad"
	private val metricsTopic = "privat-soknadInnsendt-metrics-v1-teamsoknad"

	private val kafkaMock = mockk<KafkaTemplate<String, Soknadarkivschema>>()
	private val metricsKafkaMock = mockk<KafkaTemplate<String, InnsendingMetrics>>()
	private val beskjedKafkaMock = mockk<KafkaTemplate<NokkelInput, BeskjedInput>>()
	private val oppgaveKafkaMock = mockk<KafkaTemplate<NokkelInput, OppgaveInput>>()
	private val doneKafkaMock = mockk<KafkaTemplate<NokkelInput, DoneInput>>()

	private val metrics = InnsendtMetrics(CollectorRegistry(true))
	private val receiver = mockReceiver(metrics)

	@Test
	fun `When receiving REST call, message is put on Kafka`() {
		val errorsBefore = metrics.mottattErrorGet("BIL")
		val sentInBefore = metrics.mottattSoknadGet("BIL")
		val soknad = createSoknad()

		val record = slot<ProducerRecord<String, Soknadarkivschema>>()
		val metricRecord = slot<ProducerRecord<String, InnsendingMetrics>>()

		every { kafkaMock.send(capture(record)) } returns setFuture(makeSendResult(topic, convert(soknad)))
		every { metricsKafkaMock.send(capture(metricRecord)) } returns setFuture(makeSendResult(metricsTopic, InnsendingMetrics()))

		receiver.receive(soknad)

		assertTrue(record.isCaptured)
		assertEquals(topic, record.captured.topic(), "Should send to the right topic")
		assertEquals(1, record.captured.headers().headers(MESSAGE_ID).count(), "Should have a MESSAGE_ID header")
		assertEquals("BIL", record.captured.value().arkivtema, "Should have correct tema")
		assertEquals(errorsBefore!! + 0.0, metrics.mottattErrorGet("BIL"), "Should not cause errors")
		assertEquals(sentInBefore!! + 1.0, metrics.mottattSoknadGet("BIL"), "Should increase counter by 1")

		assertEquals(metricsTopic, metricRecord.captured.topic(), "Should send metrics to the right topic")
		assertEquals(1, metricRecord.captured.headers().headers(MESSAGE_ID).count(),
			"Metrics should have a MESSAGE_ID header")
		assertEquals("soknadsmottaker", metricRecord.captured.value().application,
			"Metrics should have correct application name")
		assertEquals("publish to kafka", metricRecord.captured.value().action, "Metrics should have correct action")
		assertTrue(metricRecord.captured.value().startTime <= System.currentTimeMillis(),
			"Metrics should have correct startTime")
		assertTrue(metricRecord.captured.value().duration <= metricRecord.captured.value().startTime,
			"Metrics should have a duration")

		metrics.unregister()
	}

	@Test
	fun `Exception is thrown if message is not put on Kafka`() {
		val soknad = createSoknad()
		val metricMessage = InnsendingMetrics()

		val record = slot<ProducerRecord<String, Soknadarkivschema>>()
		val metricRecord = slot<ProducerRecord<String, InnsendingMetrics>>()

		every { kafkaMock.send(capture(record)) } throws KafkaException("Mocked Exception")
		every { metricsKafkaMock.send(capture(metricRecord)) } returns setFuture(makeSendResult(metricsTopic, metricMessage))

		assertThrows<KafkaException> {
			receiver.receive(soknad)
		}

		assertTrue(record.isCaptured)
		assertEquals(topic, record.captured.topic(), "Should send to the right topic")
		assertEquals(1, record.captured.headers().headers(MESSAGE_ID).count(), "Should have a MESSAGE_ID header")
		assertEquals("BIL", record.captured.value().arkivtema, "Should have correct tema")
		metrics.unregister()
	}

	private fun <T> makeSendResult(topic: String, melding: T) = SendResult(
			ProducerRecord(topic, "123", melding),
			RecordMetadata(TopicPartition(topic, 1), 1L, 1, 1L, 1, 1))

	private fun <T> setFuture(v: SendResult<String, T>) =
		SettableListenableFuture<SendResult<String, T>>().also { it.set(v) }

	private fun mockReceiver(metrics: InnsendtMetrics): RestApi {
		val conf = AppConfiguration()
		val kafkaSender = KafkaSender(conf, kafkaMock, metricsKafkaMock, beskjedKafkaMock, oppgaveKafkaMock, doneKafkaMock)
		val archiverService = ArchiverService(kafkaSender, metrics)
		return RestApi(archiverService)
	}
}
