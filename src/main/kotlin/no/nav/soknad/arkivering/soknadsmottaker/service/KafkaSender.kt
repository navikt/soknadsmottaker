package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.soknad.arkivering.avroschemas.Soknadarkivschema
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeaders
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class KafkaSender(private val kafkaTemplate: KafkaTemplate<String, Soknadarkivschema>) {

	private val messageId = "MESSAGE_ID"

	fun publish(topic: String, key: String, value: Soknadarkivschema) {

		val producerRecord = ProducerRecord(topic, key, value)
		val headers = RecordHeaders()
		headers.add(messageId, UUID.randomUUID().toString().toByteArray())
		headers.forEach { h -> producerRecord.headers().add(h) }

		kafkaTemplate.send(producerRecord)
	}
}
