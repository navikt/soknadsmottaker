package no.nav.soknad.arkivering.soknadsmottaker.config

import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType


object KafkaConfig2 {
	val username = Key("srvsoknadsmottaker.username", stringType)
	val password = Key("srvsoknadsmottaker.password", stringType)
	val servers = Key("kafka.bootstrap.servers", stringType)
	val secure = Key("kafka.security", stringType)
	val protocol = Key("kafka.secprot", stringType)
	val salsmec = Key("kafka.saslmec", stringType)
	val topic = Key("kafka.topic", stringType)

	val config = systemProperties() overriding
		EnvironmentVariables() overriding
		ConfigurationProperties.fromResource("application.yml")


}
