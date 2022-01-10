package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.soknad.arkivering.avroschemas.InnsendingMetrics
import no.nav.soknad.arkivering.avroschemas.Soknadarkivschema
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeaders
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

@Component
class KafkaSender(
	private val kafkaTemplate: KafkaTemplate<String, Soknadarkivschema>,
	private val metricKafkaTemplate: KafkaTemplate<String, InnsendingMetrics>
) {
	private val logger = LoggerFactory.getLogger(javaClass)

	fun publish(topic: String, key: String, value: Soknadarkivschema) {
		publish(topic, key, value, kafkaTemplate)
	}

	fun publishMetric(topic: String, key: String, value: InnsendingMetrics) {
		publish(topic, key, value, metricKafkaTemplate)
	}

	private fun <T> publish(topic: String, key: String, value: T, kafkaTemplate: KafkaTemplate<String, T>) {
		val producerRecord = ProducerRecord(topic, key, value)
		val headers = RecordHeaders()
		headers.add(MESSAGE_ID, UUID.randomUUID().toString().toByteArray())
		headers.forEach { h -> producerRecord.headers().add(h) }

		val future = kafkaTemplate.send(producerRecord)
		future.get(10, TimeUnit.SECONDS)
		logger.info("$key: Published to $topic")
	}
}


const val MESSAGE_ID = "MESSAGE_ID"
