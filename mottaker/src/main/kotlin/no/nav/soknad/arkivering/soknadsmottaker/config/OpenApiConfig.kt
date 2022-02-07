package no.nav.soknad.arkivering.soknadsmottaker.config

import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

	@Bean
	fun openApi(): OpenAPI = OpenAPI()
		.info(
			Info().title("Soknadsmottaker")
			.description("When a user applies for a benefit (_sender inn en søknad_), one or more documents are " +
				"sent in to NAV. This component acts as a REST-endpoint to which the systems that the user uses can send " +
				"metadata about the benefit. The documents themselves **must** be sent to a different system, " +
				"[Soknadsfillager](https://www.github.com/navikt/soknadsfillager), before calling Soknadsmottaker.\n" +
				"\n" +
				"When Soknadsmottaker receives data, it will be converted, serialized as an Avro message and put on a " +
				"Kafka topic.")
			.version("2.0.0")
			.contact(Contact().name("team-soknad").url("https://nav-it.slack.com/archives/C9USRUMKM"))
			.license(License().name("MIT License").url("https://github.com/navikt/soknadsmottaker/blob/main/LICENSE")))
		.externalDocs(
			ExternalDocumentation()
			.description("Documentation of the whole archiving system")
			.url("https://github.com/navikt/archiving-infrastructure/wiki"))
}
