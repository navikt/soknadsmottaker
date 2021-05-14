package no.nav.soknad.arkivering.soknadsmottaker.config

import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TraceConfig {

	@Bean
	fun httpTrace() = InMemoryHttpTraceRepository()
}
