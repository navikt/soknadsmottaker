package no.nav.soknad.arkivering.soknadsmottaker.config

import no.nav.soknad.arkivering.dto.ArchivalData
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaConfig(private val applicationProperties: ApplicationProperties) {
	private val logger = LoggerFactory.getLogger(javaClass)

	@Bean
	fun producerFactory(): ProducerFactory<String, ArchivalData> {
		val configProps = HashMap<String, Any>().also {
			it[BOOTSTRAP_SERVERS_CONFIG] = applicationProperties.kafka.bootstrapServers
			applicationProperties.kafka.envPar.getProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG)?.let {srv ->
				it[BOOTSTRAP_SERVERS_CONFIG] = srv
				logger.info("Kafka servers " + srv)
			}
			applicationProperties.kafka.envPar.getProperty(SaslConfigs.SASL_JAAS_CONFIG)?.let {sec ->
				it[SaslConfigs.SASL_JAAS_CONFIG] = sec
				logger.info("Kafka sec satt" )
			}
			it[KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
			it[VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
		}
		return DefaultKafkaProducerFactory(configProps)
	}

	@Bean
	fun kafkaTemplate() = KafkaTemplate(producerFactory())
}

