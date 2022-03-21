package no.nav.soknad.arkivering.soknadsmottaker.config

import com.natpryce.konfig.*
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import io.swagger.v3.oas.annotations.servers.Servers
import org.springframework.context.annotation.Bean
import java.io.File

private val defaultProperties = ConfigurationMap(
	mapOf(
		"KAFKA_TOPIC" to "privat-soknadInnsendt-v1-teamsoknad",
		"KAFKA_METRICS_TOPIC" to "privat-soknadInnsendt-metrics-v1-teamsoknad",
		"SCHEMA_REGISTRY_URL" to "http://localhost:8081",
		"KAFKA_BOOTSTRAP_SERVERS" to "localhost:29092",
		"KAFKA_USERNAME" to "kafkaproducer",
		"KAFKA_PASSWORD" to "",
		"KAFKA_SECURITY" to "",
		"KAFKA_SECPROT" to "",
		"KAFKA_SASLMEC" to "",

		"KAFKA_SCHEMA_REGISTRY_USER" to "",
		"KAFKA_SCHEMA_REGISTRY_PASSWORD" to "",
		"KAFKA_SCHEMA_REGISTRY" to "",
		"KAFKA_BROKERS" to "",
		"KAFKA_CA_PATH" to "",
		"KAFKA_PRIVATE_KEY_PATH" to "",
		"KAFKA_CREDSTORE_PASSWORD" to "",

		"BASICAUTH_USERNAME" to "innsending",
		"BASICAUTH_PASSWORD" to "password"
	)
)

val appConfig =
	EnvironmentVariables() overriding
		systemProperties() overriding
		ConfigurationProperties.fromResource(Configuration::class.java, "/application.yml") overriding
		ConfigurationProperties.fromResource(Configuration::class.java, "/local.properties") overriding
		defaultProperties

private fun String.configProperty(): String = appConfig[Key(this, stringType)]

fun readFileAsText(fileName: String, default: String = "") = try { File(fileName).readText(Charsets.UTF_8) } catch (e: Exception ) { default }

data class AppConfiguration(val kafkaConfig: KafkaConfig = KafkaConfig(), val restConfig: RestConfig = RestConfig()) {
	data class KafkaConfig(
		val username: String = readFileAsText("/var/run/secrets/nais.io/serviceuser/username", "KAFKA_USERNAME".configProperty()),
		val password: String = readFileAsText("/var/run/secrets/nais.io/serviceuser/password", "KAFKA_PASSWORD".configProperty()),
		val servers: String = "KAFKA_BOOTSTRAP_SERVERS".configProperty(),
		val schemaRegistryUrl: String = "SCHEMA_REGISTRY_URL".configProperty(),
		val secure: String = "KAFKA_SECURITY".configProperty(),
		val protocol: String = "KAFKA_SECPROT".configProperty(), // SASL_PLAINTEXT | SASL_SSL
		val salsmec: String = "KAFKA_SASLMEC".configProperty(), // PLAIN
		val topic: String = "KAFKA_TOPIC".configProperty(),
		val metricsTopic: String = "KAFKA_METRICS_TOPIC".configProperty(),
		val saslJaasConfig: String = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$username\" password=\"$password\";",

		// Kafka på Aiven config
		val aivenRegisteryUsername: String = "KAFKA_SCHEMA_REGISTRY_USER".configProperty(),
		val aivenRegisteryPassword: String = "KAFKA_SCHEMA_REGISTRY_PASSWORD".configProperty(),
		val aivenRegisteryUrl: String = "KAFKA_SCHEMA_REGISTRY".configProperty(),
		val aivenServers: String= "KAFKA_BROKERS".configProperty(),
		val trustStorePath: String = "KAFKA_CA_PATH".configProperty(),
		val privateKeyPath: String = "KAFKA_PRIVATE_KEY_PATH".configProperty(),
		val credstorePassword: String = "KAFKA_CREDSTORE_PASSWORD".configProperty()

		)

	data class RestConfig(
		val username: String = readFileAsText("/secrets/innsending-data/username", "BASICAUTH_USERNAME".configProperty()),
		val password: String = readFileAsText("/secrets/innsending-data/password", "BASICAUTH_PASSWORD".configProperty())
	)


	@org.springframework.context.annotation.Configuration
	class ConfigConfig {
		@Bean
		fun appConfiguration() = AppConfiguration()
	}
}
