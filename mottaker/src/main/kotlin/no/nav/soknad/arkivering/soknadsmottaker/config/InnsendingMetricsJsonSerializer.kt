package no.nav.soknad.arkivering.soknadsmottaker.config

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.soknad.arkivering.soknadsmottaker.model.InnsendingMetrics
import org.apache.kafka.common.serialization.Serializer

class InnsendingMetricsJsonSerializer : Serializer<InnsendingMetrics> {
	private val objectMapper = jacksonObjectMapper().apply {
		registerModule(JavaTimeModule())
		disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
	}

	override fun serialize(topic: String?, data: InnsendingMetrics?): ByteArray? =
		data?.let(objectMapper::writeValueAsBytes)
}
