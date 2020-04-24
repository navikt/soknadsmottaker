package no.nav.soknad.arkivering.soknadsmottaker.config

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import io.confluent.kafka.serializers.KafkaAvroSerializer
import no.nav.soknad.soknadarkivering.avroschemas.Soknadarkivschema
import org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG
import org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
class KafkaConfig {
	private val logger = LoggerFactory.getLogger(javaClass)

	fun setKafkaConfig(kafkaConfig: AppConfiguration.KafkaConfig): ProducerFactory<String, Soknadarkivschema> {
		val configProps = HashMap<String, Any>().also {
			it[BOOTSTRAP_SERVERS_CONFIG] = kafkaConfig.servers
			it[SCHEMA_REGISTRY_URL_CONFIG] = kafkaConfig.schemaRegistryUrl
			if ("TRUE" == kafkaConfig.secure) {
				it[SECURITY_PROTOCOL_CONFIG] = kafkaConfig.protocol
				it[SASL_JAAS_CONFIG] = kafkaConfig.saslJaasConfig
				it[SASL_MECHANISM] = kafkaConfig.salsmec
			}
			it[KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
			it[VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
		}

		logger.info("Ferdig med setKafkaConfig. Kafka servers=${configProps[BOOTSTRAP_SERVERS_CONFIG]}, User=${kafkaConfig.username}, profile=${kafkaConfig.profiles}, topic=${kafkaConfig.topic}")
		val password = when {
			"".equals(kafkaConfig.password, true) || "test".equals(kafkaConfig.password, true) -> kafkaConfig.password
			else -> "*Noe hemmelig fra Vault*"
		}
		logger.info("Passord='$password'")

		return DefaultKafkaProducerFactory(configProps)
	}

	@Bean
	fun producerFactory(): ProducerFactory<String, Soknadarkivschema> {
		logger.info("Start av producerFactory")
		val config = AppConfiguration()

		return setKafkaConfig(config.kafkaConfig)
	}

	@Bean
	fun kafkaTemplate() = KafkaTemplate(producerFactory())
}
