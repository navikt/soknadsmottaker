package no.nav.soknad.arkivering.soknadsmottaker.config

import no.nav.soknad.arkivering.dto.SoknadInnsendtDto
import org.apache.kafka.clients.CommonClientConfigs
import no.nav.soknad.arkivering.dto.SoknadMottattDto
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

	fun setKafkaConfig(kafkaConfig: AppConfiguration.KafkaConfig2): ProducerFactory<String, SoknadMottattDto> {
		val configProps = HashMap<String, Any>().also {
			it[BOOTSTRAP_SERVERS_CONFIG] = applicationProperties.kafka.bootstrapServers
			it[KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
			it[VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
		}
		logger.info("Slutt setKafkaConfig. Kafka servers=${kafkaConfig.servers}")
		logger.info("Miljø=${kafkaConfig.delme}")
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
