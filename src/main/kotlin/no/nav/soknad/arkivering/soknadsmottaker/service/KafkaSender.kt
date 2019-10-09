package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.soknad.arkivering.dto.ArchivalData
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaSender(private val kafkaTemplate: KafkaTemplate<String, ArchivalData>) {

	fun publish(topic: String, key: String, data: ArchivalData) {
		kafkaTemplate.send(topic, key, data)
	}
}
