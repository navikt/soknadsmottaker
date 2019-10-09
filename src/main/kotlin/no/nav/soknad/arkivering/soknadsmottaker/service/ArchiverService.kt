package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.soknad.arkivering.dto.ArchivalData
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ArchiverService(private val kafkaSender: KafkaSender) {
	private val logger = LoggerFactory.getLogger(javaClass)

	fun archive(message: String) {
		publishToKafka(message)
	}

	private fun publishToKafka(message: String) {
		val archivalData = ArchivalData(message.reversed().toUpperCase(), message)
		logger.info("Publishing to Kafka: $archivalData")
		kafkaSender.publish("archival", "key", archivalData)
	}
}
