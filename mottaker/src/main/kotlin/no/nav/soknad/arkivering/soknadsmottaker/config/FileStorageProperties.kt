package no.nav.soknad.arkivering.soknadsmottaker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "filestoragetesting")
@ConstructorBinding
data class FileStorageProperties(
	val host: String,
	val username : String,
	val password: String
)
