package no.nav.soknad.arkivering.soknadsmottaker.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.service.VendorExtension
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
 class SwaggerConfig {

	@Bean
	fun api(): Docket {
		val contact =  Contact ("Team Søknad", "https://nav-it.slack.com/messages/C9USRUMKM", "/DGNAVITTeamselvbetjening-Soknad@nav.no")
		val apiInfo = ApiInfo ("Søknadsmottak",
			"Dette er en tjeneste for Søknadsdialoger og Dokumentinnsneding sin metadata for arkivering av søknader i Joark via kafka strøm. Krever at dokumenter er lagret via filtjeneste.",
			"1.0",
			"https://nav-it.slack.com/messages/C9USRUMKM",
			contact,
			"For intern bruk",
			"http:nav.no",emptyList<VendorExtension<Any?>>())

		return Docket(DocumentationType.SWAGGER_2)
			.select()
			. apis(RequestHandlerSelectors.any())
			.paths(PathSelectors.any())
			.build()
			.apiInfo(apiInfo)
	}

}
