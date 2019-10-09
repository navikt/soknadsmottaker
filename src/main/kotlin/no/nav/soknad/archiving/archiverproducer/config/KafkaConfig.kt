package no.nav.soknad.archiving.archiverproducer.config

import no.nav.soknad.archiving.dto.ArchivalData
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaConfig(private val applicationProperties: ApplicationProperties) {

	@Bean
	fun producerFactory(): ProducerFactory<String, ArchivalData> {
		val configProps = HashMap<String, Any>().also {
			it[BOOTSTRAP_SERVERS_CONFIG] = applicationProperties.kafka.bootstrapServers
			it[KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
			it[VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
		}
		return DefaultKafkaProducerFactory(configProps)
	}

	@Bean
	fun kafkaTemplate() = KafkaTemplate(producerFactory())
}
