package no.nav.soknad.arkivering.soknadsmottaker.config

import com.natpryce.konfig.*
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.common.config.SaslConfigs
import java.io.Serializable

private val defaultProperties = ConfigurationMap(
	mapOf(
		"APP_VERSION" to "",
		"SRVSSOKNADSMOTTAKER_USERNAME" to "srvsoknadsmottaker",
		"SRVSSOKNADSMOTTAKER_PASSWORD" to "",
		"KAFKA_BOOTSTRAP_SERVERS" to "kafka-broker:29092",
		"KAFKA_CLIENTID" to "srvsoknadsmottaker",
		"KAFKA_SECURITY" to "",
		"KAFKA_SECPROT" to "",
		"KAFKA_SASLMEC" to "",
		"KAFKA_TOPIC" to "privat-soknadInnsendt-sendsoknad-v1-default",
	  "APPLICATION_PROFILE" to "",
		"DELME_TEST" to "local"
	)
)

val appConfig =
	EnvironmentVariables() overriding
		systemProperties() overriding
		ConfigurationProperties.fromResource(Configuration::class.java,"/application.yml") overriding
		ConfigurationProperties.fromResource(Configuration::class.java,"/local.properties") overriding
		defaultProperties

private fun String.configProperty(): String = appConfig[Key(this, stringType)]

data class AppConfiguration (
	val kafkaConfig: KafkaConfig2 = KafkaConfig2()
) {
	data class KafkaConfig2 (
		val profile: String = "APPLICATION_PROFILE".configProperty(),
		val version: String = "APP_VERSION".configProperty(),
		val username: String = "SRVSSOKNADSMOTTAKER_USERNAME".configProperty(),
		val password: String = "SRVSSOKNADSMOTTAKER_PASSWORD".configProperty(),
		val servers: String = "KAFKA_BOOTSTRAP_SERVERS".configProperty(),
		val clientId: String = "KAFKA_CLIENTID".configProperty(),
		val secure: String = "KAFKA_SECURITY".configProperty(),
		val protocol: String = "KAFKA_SECPROT".configProperty(),
		val salsmec: String = "KAFKA_SASLMEC".configProperty(),
		val topic: String = "KAFKA_TOPIC".configProperty(),
		val saslJaasConfig: String = "org.apache.kafka.common.security.plain.PlainLoginModule required " + "username=\"$username\" password=\"$password\";",
		val delme: String = "DELME_TEST".configProperty()
	)
}
