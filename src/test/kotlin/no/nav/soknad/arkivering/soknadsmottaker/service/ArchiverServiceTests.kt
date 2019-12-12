package no.nav.soknad.arkivering.soknadsmottaker.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import no.nav.soknad.arkivering.dto.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ArchiverServiceTests {

	private val kafkaSender = mock<KafkaSender> { }
	private val archiverService = ArchiverService(kafkaSender)
	private val eksternReferanseIDBil = "123445666"
	private val personIdBIL = "12345678910"


	@Test
	fun `Kaller Kafka sender`() {
		archiverService.archive(opprettMeldingPaKafka())

		verify(kafkaSender, times(1)).publish(any(), any(), any())
	}

	private fun opprettMeldingPaKafka() =
		SoknadMottattDto(eksternReferanseIDBil,personIdBIL,"FNR","BIL", LocalDateTime.now(), listOf())
}
