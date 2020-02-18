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

	fun setKafkaConfig(kafkaConfig: AppConfiguration.KafkaConfig2): ProducerFactory<String, ArchivalData> {
		logger.info("Start setKafkaConfig")
		val configProps = HashMap<String, Any>().also {
			it[BOOTSTRAP_SERVERS_CONFIG] = kafkaConfig.servers
			it[SaslConfigs.SASL_JAAS_CONFIG] = kafkaConfig.saslJaasConfig
			it[KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
			it[VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
		}
		logger.info("Slutt setKafkaConfig. Kafka servers=${kafkaConfig.servers}")
		logger.info("Miljø=${kafkaConfig.delme}")
		return DefaultKafkaProducerFactory(configProps)
	}

	@Bean
	fun producerFactory(): ProducerFactory<String, ArchivalData> {
		logger.info("Start av producerFactory")
		val config = AppConfiguration()

		return setKafkaConfig(config.kafkaConfig)
	}

	@Bean
	fun kafkaTemplate() = KafkaTemplate(producerFactory())
}

