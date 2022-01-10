package no.nav.soknad.arkivering.soknadsmottaker.config

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import io.confluent.kafka.serializers.KafkaAvroSerializer
import no.nav.soknad.arkivering.avroschemas.InnsendingMetrics
import no.nav.soknad.arkivering.avroschemas.Soknadarkivschema
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

@Configuration
class KafkaConfig(private val appConfiguration: AppConfiguration) {
	private val logger = LoggerFactory.getLogger(javaClass)

	fun getKafkaConfig(): HashMap<String, Any> {
		val config = appConfiguration.kafkaConfig

		val configProps = HashMap<String, Any>().also {
			it[BOOTSTRAP_SERVERS_CONFIG] = config.servers
			it[SCHEMA_REGISTRY_URL_CONFIG] = config.schemaRegistryUrl
			if ("TRUE" == config.secure) {
				it[SECURITY_PROTOCOL_CONFIG] = config.protocol
				it[SASL_JAAS_CONFIG] = config.saslJaasConfig
				it[SASL_MECHANISM] = config.salsmec
			}
			it[KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
			it[VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
		}

		val password = when {
			config.password == "" || "test".equals(config.password, true) -> config.password
			else -> "*Secret from Vault*"
		}
		logger.info("Will use the following KafkaConfig: Bootstrap servers='${configProps[BOOTSTRAP_SERVERS_CONFIG]}', " +
			"User='${config.username}', password='$password', topic='${config.topic}'")

		return configProps
	}

	@Bean
	fun producerFactory() = DefaultKafkaProducerFactory<String, Soknadarkivschema>(getKafkaConfig())

	@Bean
	fun metricProducerFactory() = DefaultKafkaProducerFactory<String, InnsendingMetrics>(getKafkaConfig())

	@Bean
	fun kafkaTemplate() = KafkaTemplate(producerFactory())

	@Bean
	fun metricKafkaTemplate() = KafkaTemplate(metricProducerFactory())
}
