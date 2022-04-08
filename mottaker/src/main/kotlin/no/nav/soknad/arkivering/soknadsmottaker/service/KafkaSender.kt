package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.brukernotifikasjon.schemas.input.BeskjedInput
import no.nav.brukernotifikasjon.schemas.input.DoneInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.brukernotifikasjon.schemas.input.OppgaveInput
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
	private val metricKafkaTemplate: KafkaTemplate<String, InnsendingMetrics>,
	private val kafkaBeskjedTemplate: KafkaTemplate<NokkelInput, BeskjedInput>,
	private val kafkaOppgaveTemplate: KafkaTemplate<NokkelInput, OppgaveInput>,
	private val kafkaDoneTemplate: KafkaTemplate<NokkelInput, DoneInput>,
) {
	private val logger = LoggerFactory.getLogger(javaClass)

	fun publish(topic: String, key: String, value: Soknadarkivschema) {
		publish(topic, key, value, kafkaTemplate)
		logger.info("$key: Published to $topic")
	}

	fun publishMetric(topic: String, key: String, value: InnsendingMetrics) {
		publish(topic, key, value, metricKafkaTemplate)
		logger.info("$key: Published to $topic")
	}

	fun publishDoneNotification(key: NokkelInput, value: DoneInput) {
		publishBrukernotifikasjon("min-side.aapen-brukernotifikasjon-done-v1", key, value, kafkaDoneTemplate)
	}

	fun publishBeskjedNotification(key: NokkelInput, value: BeskjedInput) {
		publishBrukernotifikasjon("min-side.aapen-brukernotifikasjon-beskjed-v1", key, value, kafkaBeskjedTemplate)
	}

	fun publishOppgaveNotification(key: NokkelInput, value: OppgaveInput) {
		publishBrukernotifikasjon("min-side.aapen-brukernotifikasjon-oppgave-v1", key, value, kafkaOppgaveTemplate)
	}

	private fun <T> publishBrukernotifikasjon(topic: String, key: NokkelInput, value: T, kafkaTemplate: KafkaTemplate<NokkelInput, T>) {
		logger.info("${key.getGrupperingsId()}: Skal publisere notifikasjon med eventId=${key.getEventId()} på topic $topic")
		publish(topic, key, value, kafkaTemplate)
		logger.info("${key.getGrupperingsId()}: Published to $topic")
	}

	private fun <K, V> publish(topic: String, key: K, value: V, kafkaTemplate: KafkaTemplate<K, V>) {
		val producerRecord = ProducerRecord(topic, key, value)
		val headers = RecordHeaders()
		headers.add(MESSAGE_ID, UUID.randomUUID().toString().toByteArray())
		headers.forEach { h -> producerRecord.headers().add(h) }

		val future = kafkaTemplate.send(producerRecord)
		future.get(10, TimeUnit.SECONDS)
	}
}


const val MESSAGE_ID = "MESSAGE_ID"
