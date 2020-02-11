package no.nav.soknad.arkivering.soknadsmottaker.config

import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.common.config.SaslConfigs
import java.io.Serializable
import java.util.*


object KafkaConfig2 {
	val username = Key("srvsoknadsmottaker.username", stringType)
	val password = Key("srvsoknadsmottaker.password", stringType)
	val servers = Key("kafka.bootstrap.servers", stringType)
	val secure = Key("kafka.security", stringType)
	val protocol = Key("kafka.secprot", stringType)
	val salsmec = Key("kafka.saslmec", stringType)
	val topic = Key("kafka.topic", stringType)

	val appConfig = if (System.getenv("APPLICATION_PROFILE") == "remote") {
		EnvironmentVariables() overriding
			systemProperties() overriding
			ConfigurationProperties.fromResource("application.yml")
	} else {
		EnvironmentVariables() overriding
			systemProperties()
	}

	val config = Properties().apply {
		appConfig.getOrNull(servers)?.let {
			setProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, it)
		}
		if (appConfig.contains(username) && appConfig.contains(password)) {
			setProperty(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required " +
				"username=\"${appConfig[username]}\" password=\"${appConfig[password]}\";")

			if (System.getenv("APPLICATION_PROFILE") != "remote")
				setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT")

		}
	}

	internal fun Map<String, Serializable>.addKafkaSecurity(
		username: String,
		password: String,
		secProtocol: String = "SASL_PLAINTEXT",
		saslMechanism: String = "PLAIN"
	): Map<String, Any> = this.let {

		val mMap = this.toMutableMap()

		mMap[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = secProtocol
		mMap[SaslConfigs.SASL_MECHANISM] = saslMechanism

		val jaasPainLogin = "org.apache.kafka.common.security.plain.PlainLoginModule"
		val jaasRequired = "required"

		mMap[SaslConfigs.SASL_JAAS_CONFIG] = "$jaasPainLogin $jaasRequired " +
			"username=\"$username\" password=\"$password\";"

		mMap.toMap()
	}
}
