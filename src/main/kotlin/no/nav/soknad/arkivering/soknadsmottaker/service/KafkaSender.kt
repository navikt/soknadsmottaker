package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.soknad.soknadarkivering.avroschemas.Soknadarkivschema
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaSender(private val kafkaTemplate: KafkaTemplate<String, Soknadarkivschema>) {

	fun publish(topic: String, key: String, data: Soknadarkivschema) {
		kafkaTemplate.send(topic, key, data)
	}
}
