package no.nav.soknad.arkivering.soknadsmottaker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class SoknadsmottakerApplication

fun main(args: Array<String>) {
	runApplication<SoknadsmottakerApplication>(*args)
}
