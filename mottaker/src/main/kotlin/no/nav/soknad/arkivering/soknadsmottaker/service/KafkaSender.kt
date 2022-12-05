package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.brukernotifikasjon.schemas.input.BeskjedInput
import no.nav.brukernotifikasjon.schemas.input.DoneInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.brukernotifikasjon.schemas.input.OppgaveInput
import no.nav.soknad.arkivering.avroschemas.InnsendingMetrics
import no.nav.soknad.arkivering.avroschemas.Soknadarkivschema
import no.nav.soknad.arkivering.soknadsmottaker.config.KafkaConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

@Component
class KafkaSender(
	private val kafkaConfig: KafkaConfig,
	private val kafkaTemplate: KafkaTemplate<String, Soknadarkivschema>,
	private val metricKafkaTemplate: KafkaTemplate<String, InnsendingMetrics>,
	private val kafkaBeskjedTemplate: KafkaTemplate<NokkelInput, BeskjedInput>,
	private val kafkaOppgaveTemplate: KafkaTemplate<NokkelInput, OppgaveInput>,
	private val kafkaDoneTemplate: KafkaTemplate<NokkelInput, DoneInput>,
	private val kafkaUtkastTemplate: KafkaTemplate<String, String>
) {
	private val logger = LoggerFactory.getLogger(javaClass)

	fun publishSoknadarkivschema(key: String, value: Soknadarkivschema) {
		val topic = kafkaConfig.mainTopic
		publish(topic, key, value, kafkaTemplate)
		logger.info("$key: Published to $topic")
	}

	fun publishMetric(key: String, value: InnsendingMetrics) {
		val topic = kafkaConfig.metricsTopic
		publish(topic, key, value, metricKafkaTemplate)
		logger.info("$key: Published to $topic")
	}

	fun publishDoneNotification(key: NokkelInput, value: DoneInput) {
		val topic = kafkaConfig.brukernotifikasjonDoneTopic
		publishBrukernotifikasjon(topic, key, value, kafkaDoneTemplate)
	}

	fun publishUtkastNotification(key: String, value: String) {
		val topic = kafkaConfig.utkastTopic
		try {
			//publish(topic, key, value, kafkaUtkastTemplate)
			logger.debug("$key. Skal publisere Utkast $value")
			val props = Properties()
			props[KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.qualifiedName
			props[VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.qualifiedName

			KafkaProducer<String, String>(props).use { producer ->
				producer.send(ProducerRecord(topic, key, value)) {
					m: RecordMetadata, e: Exception? ->
					when (e) {
						// no exception, good to go!
						null -> println("Produced record to topic ${m.topic()} partition [${m.partition()}] @ offset ${m.offset()}")
						// print stacktrace in case of exception
						else -> logger.error("$key: Feil ved publisering til $topic, ${e.message}")
					}
				}
				producer.flush()
			}

		} catch (ex: Exception) {
			logger.warn("$key: Feil ved publisering av utkast, ${ex.message}")
		}
	}

	fun publishBeskjedNotification(key: NokkelInput, value: BeskjedInput) {
		val topic = kafkaConfig.brukernotifikasjonBeskjedTopic
		publishBrukernotifikasjon(topic, key, value, kafkaBeskjedTemplate)
	}

	fun publishOppgaveNotification(key: NokkelInput, value: OppgaveInput) {
		val topic = kafkaConfig.brukernotifikasjonOppgaveTopic
		publishBrukernotifikasjon(topic, key, value, kafkaOppgaveTemplate)
	}

	private fun <T> publishBrukernotifikasjon(topic: String, key: NokkelInput, value: T, kafkaTemplate: KafkaTemplate<NokkelInput, T>) {
		logger.info("${key.grupperingsId}: Skal publisere notifikasjon med eventId=${key.eventId} på topic $topic")
		publish(topic, key, value, kafkaTemplate)
		logger.info("${key.grupperingsId}: Published to $topic")
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
