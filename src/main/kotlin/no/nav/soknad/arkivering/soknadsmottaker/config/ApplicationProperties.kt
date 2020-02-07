package no.nav.soknad.arkivering.soknadsmottaker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("application")
class ApplicationProperties {
	var kafka = Kafka()

	class Kafka {
		var envPar = KafkaConfig2.config

		lateinit var bootstrapServers: String

	}
}
