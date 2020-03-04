package no.nav.soknad.arkivering.soknadsmottaker.config

import no.nav.soknad.arkivering.dto.SoknadMottattDto
import org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaConfig() {
	private val logger = LoggerFactory.getLogger(javaClass)

	fun setKafkaConfig(kafkaConfig: AppConfiguration.KafkaConfig): ProducerFactory<String, SoknadMottattDto> {
		val configProps = HashMap<String, Any>().also {
			it[BOOTSTRAP_SERVERS_CONFIG] = kafkaConfig.servers
			it[SECURITY_PROTOCOL_CONFIG] = kafkaConfig.protocol
			it[SASL_JAAS_CONFIG] = kafkaConfig.saslJaasConfig
			it[KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
			it[VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
			it["TOPIC"] = kafkaConfig.topic
		}
		logger.info("Slutt setKafkaConfig." +
			" Kafka servers=${configProps.get(BOOTSTRAP_SERVERS_CONFIG)}, User=${kafkaConfig.username}, profile=${kafkaConfig.profiles}, topic=${kafkaConfig.topic}")
		logger.info("Passord="+
			( when {
				"".equals(kafkaConfig.password, true) || "test".equals(kafkaConfig.password, true) -> kafkaConfig.password
				else -> "*Noe hemmelig fra Vault*"
			}))
		return DefaultKafkaProducerFactory(configProps)
	}

	@Bean
	fun producerFactory(): ProducerFactory<String, SoknadMottattDto> {
		logger.info("Start av producerFactory")
		val config = AppConfiguration()

		return setKafkaConfig(config.kafkaConfig)
	}

	@Bean
	fun kafkaTemplate() = KafkaTemplate(producerFactory())
}

/*
acks=all
client.id=soknadInnsendt-sendsoknad
enable.idempotence=true
max.in.flight.requests.per.connection=1
max.block.ms=15000
retries=2
key.serializer=io.confluent.kafka.serializers.KafkaAvroSerializer
value.serializer=io.confluent.kafka.serializers.KafkaAvroSerializer
bootstrap.servers=localhost:9092
schema.registry.url=http://kafka-schema-registry.tpa:8081
security.protocol=SASL_SSL
sasl.mechanism=PLAIN
compression.type=gzip
max.request.size=15728640
*/
