package no.nav.soknad.arkivering.soknadsmottaker.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/*
Problem med Jackson and Kotlin data classes  i forbindelse med testing av overgang fra spring boot 3.x til 4.x
 */
@Configuration
class JacksonConfig : WebMvcConfigurer {

	@Bean
	fun objectMapper(): ObjectMapper {
		val mapper =  jacksonObjectMapper().apply {
			registerModule(JavaTimeModule())
			// Valgfritt: Hindre at datoer skrives som tall-arrays/timestamps
			disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
		}
		mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
		mapper.registerModule(KotlinModule.Builder().build())
		return mapper
	}
}
