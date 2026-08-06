package no.nav.soknad.arkivering.soknadsmottaker.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.soknad.arkivering.soknadsmottaker.model.InnsendingMetrics
import org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset

class InnsendingMetricsJsonSerializerTest {
	private val startTime = OffsetDateTime.of(2026, 8, 6, 10, 30, 15, 0, ZoneOffset.UTC)
	private val metric = InnsendingMetrics(
		application = "soknadsmottaker",
		action = "publish to kafka",
		startTime = startTime,
		duration = 123L
	)

	@Test
	fun `serializes generated metrics model as plain JSON`() {
		val serialized = InnsendingMetricsJsonSerializer().serialize("metrics-v3", metric)
		val json = jacksonObjectMapper().readTree(serialized)

		assertEquals("soknadsmottaker", json["application"].asText())
		assertEquals("publish to kafka", json["action"].asText())
		assertEquals("2026-08-06T10:30:15Z", json["startTime"].asText())
		assertEquals(123L, json["duration"].asLong())
		assertEquals(4, json.size())
	}

	@Test
	fun `serializes null as a Kafka tombstone`() {
		assertNull(InnsendingMetricsJsonSerializer().serialize("metrics-v3", null))
	}

	@Test
	fun `configures String keys and JSON metrics values`() {
		val properties = KafkaSetup(kafkaConfig()).metricProducerFactory().configurationProperties

		assertEquals(StringSerializer::class.java, properties[KEY_SERIALIZER_CLASS_CONFIG])
		assertEquals(InnsendingMetricsJsonSerializer::class.java, properties[VALUE_SERIALIZER_CLASS_CONFIG])
	}

	private fun kafkaConfig() = KafkaConfig().apply {
		namespace = "team-soknad"
		secure = "FALSE"
		schemaRegistryUsername = "user"
		schemaRegistryPassword = "password"
		schemaRegistryUrl = "http://localhost:8081"
		kafkaBrokers = "localhost:29092"
		truststorePath = "truststore"
		keystorePath = "keystore"
		credstorePassword = "password"
		metricsTopic = "privat-soknadinnsending-metrics-v3-dev"
	}
}
