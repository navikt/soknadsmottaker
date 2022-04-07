package no.nav.soknad.arkivering.soknadsmottaker.config

import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import no.nav.brukernotifikasjon.schemas.input.BeskjedInput
import no.nav.brukernotifikasjon.schemas.input.DoneInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.brukernotifikasjon.schemas.input.OppgaveInput
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate

@Configuration
class UserNotificationConfig(private val appConfiguration: AppConfiguration) {

	fun getKafkaAivenConfig(): HashMap<String, Any> {
		val appConfig = appConfiguration.kafkaConfig

		val configProps = java.util.HashMap<String, Any>().also {
			it[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = appConfig.aivenServers
			it[KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = appConfig.aivenRegisteryUrl
			it[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
			it[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
			it[ProducerConfig.MAX_BLOCK_MS_CONFIG] = 30000
			it[ProducerConfig.ACKS_CONFIG] = "all"
			it[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG] = "false"
			if (appConfig.secure == "TRUE") {
				it[KafkaAvroSerializerConfig.USER_INFO_CONFIG] = "${appConfig.aivenRegisteryUsername}:${appConfig.aivenRegisteryPassword}"
				it[KafkaAvroSerializerConfig.BASIC_AUTH_CREDENTIALS_SOURCE] = "USER_INFO"
				it[SaslConfigs.SASL_MECHANISM] = "PLAIN"
				it[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SSL"
				it[SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG] = "jks"
				it[SslConfigs.SSL_KEYSTORE_TYPE_CONFIG] = "PKCS12"
				it[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = appConfig.credstorePassword
				it[SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = appConfig.credstorePassword
				it[SslConfigs.SSL_KEY_PASSWORD_CONFIG] = appConfig.credstorePassword
				it[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = appConfig.trustStorePath
				it[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = appConfig.privateKeyPath
				it[SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG] =  ""
			}
		}

		return configProps
	}

	@Bean
	fun defaultBeskjedNotificationFactory() = DefaultKafkaProducerFactory<NokkelInput, BeskjedInput>(getKafkaAivenConfig())

	@Bean
	fun defaultOppgaveNotificationFactory() = DefaultKafkaProducerFactory<NokkelInput, OppgaveInput>(getKafkaAivenConfig())

	@Bean
	fun defaultDoneNotificationFactory() = DefaultKafkaProducerFactory<NokkelInput, DoneInput>(getKafkaAivenConfig())

	@Bean
	fun kafkaBeskjedTemplate() = KafkaTemplate(defaultBeskjedNotificationFactory())

	@Bean
	fun kafkaOppgaveTemplate() = KafkaTemplate(defaultOppgaveNotificationFactory())

	@Bean
	fun kafkaDoneTemplate() = KafkaTemplate(defaultDoneNotificationFactory())
}
