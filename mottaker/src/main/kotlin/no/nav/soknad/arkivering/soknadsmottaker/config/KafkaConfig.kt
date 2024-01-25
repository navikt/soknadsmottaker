package no.nav.soknad.arkivering.soknadsmottaker.config

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig.BASIC_AUTH_CREDENTIALS_SOURCE
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig.USER_INFO_CONFIG
import no.nav.soknad.arkivering.avroschemas.InnsendingMetrics
import no.nav.soknad.arkivering.avroschemas.Soknadarkivschema
import org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM
import org.apache.kafka.common.config.SslConfigs.*
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate

@Configuration
class KafkaSetup(private val kafkaConfig: KafkaConfig) {
	private val stringKeySerializerClass = StringSerializer::class.java
	private val stringValueSerializerClass = StringSerializer::class.java

	fun <T : Serializer<*>> createKafkaConfig(keySerializer: Class<T>, valueSerializer: Class<T>? = null): HashMap<String, Any> {

		return HashMap<String, Any>().also {
			it[BOOTSTRAP_SERVERS_CONFIG] = kafkaConfig.kafkaBrokers
			it[SCHEMA_REGISTRY_URL_CONFIG] = kafkaConfig.schemaRegistryUrl
			it[KEY_SERIALIZER_CLASS_CONFIG] = keySerializer
			it[VALUE_SERIALIZER_CLASS_CONFIG] = valueSerializer ?: KafkaAvroSerializer::class.java
			it[MAX_BLOCK_MS_CONFIG] = 30000
			it[ACKS_CONFIG] = "all"
			it[ENABLE_IDEMPOTENCE_CONFIG] = "false"
			if (kafkaConfig.secure == "TRUE") {
				it[USER_INFO_CONFIG] = "${kafkaConfig.schemaRegistryUsername}:${kafkaConfig.schemaRegistryPassword}"
				it[BASIC_AUTH_CREDENTIALS_SOURCE] = "USER_INFO"
				it[SASL_MECHANISM] = "PLAIN"
				it[SECURITY_PROTOCOL_CONFIG] = "SSL"
				it[SSL_TRUSTSTORE_TYPE_CONFIG] = "jks"
				it[SSL_KEYSTORE_TYPE_CONFIG] = "PKCS12"
				it[SSL_TRUSTSTORE_PASSWORD_CONFIG] = kafkaConfig.credstorePassword
				it[SSL_KEYSTORE_PASSWORD_CONFIG] = kafkaConfig.credstorePassword
				it[SSL_KEY_PASSWORD_CONFIG] = kafkaConfig.credstorePassword
				it[SSL_TRUSTSTORE_LOCATION_CONFIG] = kafkaConfig.truststorePath
				it[SSL_KEYSTORE_LOCATION_CONFIG] = kafkaConfig.keystorePath
				it[SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG] =  ""
			}
		}
	}

	@Bean
	fun producerFactory() = DefaultKafkaProducerFactory<String, Soknadarkivschema>(createKafkaConfig(stringKeySerializerClass))

	@Bean
	fun metricProducerFactory() = DefaultKafkaProducerFactory<String, InnsendingMetrics>(createKafkaConfig(stringKeySerializerClass))

	@Bean
	fun beskjedNotificationFactory() = DefaultKafkaProducerFactory<String, String>(createKafkaConfig(stringKeySerializerClass, stringValueSerializerClass))

	@Bean
	fun oppgaveNotificationFactory() = DefaultKafkaProducerFactory<String, String>(createKafkaConfig(stringKeySerializerClass, stringValueSerializerClass))

	@Bean
	fun doneNotificationFactory() = DefaultKafkaProducerFactory<String, String>(createKafkaConfig(stringKeySerializerClass, stringValueSerializerClass))

	@Bean
	fun utkastFactory() = DefaultKafkaProducerFactory<String, String>(createKafkaConfig(stringKeySerializerClass, stringValueSerializerClass))

	@Bean
	fun kafkaBeskjedTemplate() = KafkaTemplate(beskjedNotificationFactory())

	@Bean
	fun kafkaOppgaveTemplate() = KafkaTemplate(oppgaveNotificationFactory())

	@Bean
	fun kafkaDoneTemplate() = KafkaTemplate(doneNotificationFactory())

	@Bean
	fun kafkaUtkastTemplate() = KafkaTemplate(utkastFactory())

	@Bean
	fun kafkaTemplate() = KafkaTemplate(producerFactory())

	@Bean
	fun metricKafkaTemplate() = KafkaTemplate(metricProducerFactory())
}

@ConfigurationProperties("kafkaconfig")
class KafkaConfig {
	lateinit var namespace: String
	lateinit var secure: String
	lateinit var schemaRegistryUsername: String
	lateinit var schemaRegistryPassword: String
	lateinit var schemaRegistryUrl: String
	lateinit var kafkaBrokers: String
	lateinit var truststorePath: String
	lateinit var keystorePath: String
	lateinit var credstorePassword: String

	lateinit var mainTopic: String
	lateinit var metricsTopic: String
	lateinit var brukernotifikasjonDoneTopic: String
	lateinit var brukernotifikasjonBeskjedTopic: String
	lateinit var brukernotifikasjonOppgaveTopic: String
	lateinit var utkastTopic: String
}
