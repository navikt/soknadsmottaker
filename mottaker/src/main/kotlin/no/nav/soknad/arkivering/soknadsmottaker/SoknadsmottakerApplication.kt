package no.nav.soknad.arkivering.soknadsmottaker

import org.openapitools.SpringDocConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import
import org.springframework.resilience.annotation.EnableResilientMethods

@Import(SpringDocConfiguration::class)
@SpringBootApplication()
@ConfigurationPropertiesScan
@EnableResilientMethods
class SoknadsmottakerApplication

fun main(args: Array<String>) {
	runApplication<SoknadsmottakerApplication>(*args)
}
