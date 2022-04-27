package no.nav.soknad.arkivering.soknadsmottaker.config

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig.BASIC_AUTH_CREDENTIALS_SOURCE
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig.USER_INFO_CONFIG
import no.nav.brukernotifikasjon.schemas.input.BeskjedInput
import no.nav.brukernotifikasjon.schemas.input.DoneInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.brukernotifikasjon.schemas.input.OppgaveInput
import no.nav.soknad.arkivering.avroschemas.InnsendingMetrics
import no.nav.soknad.arkivering.avroschemas.Soknadarkivschema
import org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM
import org.apache.kafka.common.config.SslConfigs.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate

@Configuration
class KafkaConfig(private val appConfiguration: AppConfiguration) {

	fun getKafkaConfig(): HashMap<String, Any> {
		val appConfig = appConfiguration.kafkaConfig

		return HashMap<String, Any>().also {
			it[BOOTSTRAP_SERVERS_CONFIG] = appConfig.kafkaBrokers
			it[SCHEMA_REGISTRY_URL_CONFIG] = appConfig.schemaRegistryUrl
			it[KEY_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
			it[VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
			it[MAX_BLOCK_MS_CONFIG] = 30000
			it[ACKS_CONFIG] = "all"
			it[ENABLE_IDEMPOTENCE_CONFIG] = "false"
			if (appConfig.secure == "TRUE") {
				it[USER_INFO_CONFIG] = "${appConfig.schemaRegistryUsername}:${appConfig.schemaRegistryPassword}"
				it[BASIC_AUTH_CREDENTIALS_SOURCE] = "USER_INFO"
				it[SASL_MECHANISM] = "PLAIN"
				it[SECURITY_PROTOCOL_CONFIG] = "SSL"
				it[SSL_TRUSTSTORE_TYPE_CONFIG] = "jks"
				it[SSL_KEYSTORE_TYPE_CONFIG] = "PKCS12"
				it[SSL_TRUSTSTORE_PASSWORD_CONFIG] = appConfig.credstorePassword
				it[SSL_KEYSTORE_PASSWORD_CONFIG] = appConfig.credstorePassword
				it[SSL_KEY_PASSWORD_CONFIG] = appConfig.credstorePassword
				it[SSL_TRUSTSTORE_LOCATION_CONFIG] = appConfig.truststorePath
				it[SSL_KEYSTORE_LOCATION_CONFIG] = appConfig.keystorePath
				it[SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG] =  ""
			}
		}
	}

	@Bean
	fun producerFactory() = DefaultKafkaProducerFactory<String, Soknadarkivschema>(getKafkaConfig())

	@Bean
	fun metricProducerFactory() = DefaultKafkaProducerFactory<String, InnsendingMetrics>(getKafkaConfig())

	@Bean
	fun defaultBeskjedNotificationFactory() = DefaultKafkaProducerFactory<NokkelInput, BeskjedInput>(getKafkaConfig())

	@Bean
	fun defaultOppgaveNotificationFactory() = DefaultKafkaProducerFactory<NokkelInput, OppgaveInput>(getKafkaConfig())

	@Bean
	fun defaultDoneNotificationFactory() = DefaultKafkaProducerFactory<NokkelInput, DoneInput>(getKafkaConfig())

	@Bean
	fun kafkaBeskjedTemplate() = KafkaTemplate(defaultBeskjedNotificationFactory())

	@Bean
	fun kafkaOppgaveTemplate() = KafkaTemplate(defaultOppgaveNotificationFactory())

	@Bean
	fun kafkaDoneTemplate() = KafkaTemplate(defaultDoneNotificationFactory())

	@Bean
	fun kafkaTemplate() = KafkaTemplate(producerFactory())

	@Bean
	fun metricKafkaTemplate() = KafkaTemplate(metricProducerFactory())
}
