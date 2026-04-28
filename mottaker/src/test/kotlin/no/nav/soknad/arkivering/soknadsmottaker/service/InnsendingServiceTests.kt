package no.nav.soknad.arkivering.soknadsmottaker.service

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.soknad.arkivering.soknadsmottaker.model.AvsenderDto
import no.nav.soknad.arkivering.soknadsmottaker.model.BrukerDto
import no.nav.soknad.arkivering.soknadsmottaker.supervision.InnsendtMetrics
import no.nav.soknad.arkivering.soknadsmottaker.utils.createInnsending
import org.junit.jupiter.api.Test

class InnsendingServiceTests {
	private val kafkaSender = mockk<KafkaSender>()

	private val metrics = mockk<InnsendtMetrics>(relaxed = true)

	private val mapper =  jacksonObjectMapper().apply {
		registerModule(JavaTimeModule())
		// Valgfritt: Hindre at datoer skrives som tall-arrays/timestamps
		disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
	}


	@Test
	fun `Calls Kafka sender - loggedin`() {
		every { kafkaSender.publishSubmission(any(), any(), true) } returns Unit
		every { kafkaSender.publishMetric(any(), any()) } returns Unit

		val soknad = createInnsending(
			brukerDto = BrukerDto("01234567891", BrukerDto.IdType.FNR),
			avsenderDto = AvsenderDto(
				id = "01234567891",
				idType = AvsenderDto.IdType.FNR,
				navn = null
			), kanal = "NAV_NO", tema = "HJE"
		)

		val innsendingService = InnsendingService(kafkaSender, metrics, mapper)

		innsendingService.publishToLoggedinTopic(soknad.innsendingsId, soknad)

		verify { kafkaSender.publishSubmission(any(), any(), soknad.kanal == "NAV_NO") }
		verify { kafkaSender.publishMetric(any(), any()) }
	}

	@Test
	fun `Calls Kafka sender - nologin`() {
		every { kafkaSender.publishSubmission(any(), any(), false) } returns Unit
		every { kafkaSender.publishMetric(any(), any()) } returns Unit

		val soknad = createInnsending(
			brukerDto = BrukerDto("01234567891", BrukerDto.IdType.FNR),
			avsenderDto = AvsenderDto(
				id = "01234567891",
				idType = AvsenderDto.IdType.FNR,
				navn = null
			), kanal = "NAV_NO_UINNLOGGET", tema = "HJE"
		)

		val innsendingService = InnsendingService(kafkaSender, metrics, mapper)

		innsendingService.publishToNoLoginTopic(soknad.innsendingsId, soknad)

		verify { kafkaSender.publishSubmission(any(), any(), false) }
		verify { kafkaSender.publishMetric(any(), any()) }
	}
}
