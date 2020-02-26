package no.nav.soknad.arkivering.soknadsmottaker.config

import no.nav.soknad.soknadarkivering.avroschemas.Soknadarkivschema
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
class KafkaConfig(private val applicationProperties: ApplicationProperties) {

	@Bean
	fun producerFactory(): ProducerFactory<String, Soknadarkivschema> {
		val configProps = HashMap<String, Any>().also {
			it[BOOTSTRAP_SERVERS_CONFIG] = applicationProperties.kafka.bootstrapServers
			it[KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
			it[VALUE_SERIALIZER_CLASS_CONFIG] = AvroSerializer::class.java
		}
		return DefaultKafkaProducerFactory(configProps)
	}

	@Bean
	fun kafkaTemplate() = KafkaTemplate(producerFactory())
}
