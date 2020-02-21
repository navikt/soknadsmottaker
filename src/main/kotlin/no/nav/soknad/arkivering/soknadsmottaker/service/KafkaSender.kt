package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.soknad.arkivering.dto.SoknadMottattDto
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaSender(private val kafkaTemplate: KafkaTemplate<String, SoknadMottattDto>) {

	fun publish(topic: String, key: String, data: SoknadMottattDto) {
		kafkaTemplate.send(topic, key, data)
	}
}
