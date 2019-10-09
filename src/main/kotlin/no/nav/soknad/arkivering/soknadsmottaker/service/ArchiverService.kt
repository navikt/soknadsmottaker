package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.soknad.arkivering.dto.ArchivalData
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ArchiverService(private val kafkaSender: KafkaSender) {
	private val logger = LoggerFactory.getLogger(javaClass)

	fun archive(message: String) {
		val archivalData = createKafkaMessage(message)
		publishToKafka(archivalData)
	}

	private fun createKafkaMessage(message: String) = ArchivalData(message.reversed().toUpperCase(), message)

	private fun publishToKafka(archivalData: ArchivalData) {
		logger.info("Publishing to Kafka: $archivalData")
		kafkaSender.publish("archival", "key", archivalData)
	}
}
