package no.nav.soknad.arkivering.soknadsmottaker.config

import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import org.apache.kafka.clients.CommonClientConfigs
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
		appConfig.getOrNull(KafkaConfig2.servers)?.let {
			setProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, it)
		}
	}

}
