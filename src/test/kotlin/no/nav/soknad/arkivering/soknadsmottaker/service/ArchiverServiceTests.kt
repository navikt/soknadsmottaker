package no.nav.soknad.arkivering.soknadsmottaker.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import no.nav.soknad.arkivering.dto.InnsendtDokumentDto
import no.nav.soknad.arkivering.dto.SoknadInnsendtDto
import org.joda.time.DateTime
import org.junit.jupiter.api.Test
import java.util.*

class ArchiverServiceTests {

	private val kafkaSender = mock<KafkaSender> { }
	private val archiverService = ArchiverService(kafkaSender)

	@Test
	fun `Will call Kafka Sender`() {
		archiverService.archive(createMessage())

		verify(kafkaSender, times(1)).publish(any(), any(), any())
	}

	private fun createMessage(): SoknadInnsendtDto {
		val innsendtDokumentDto =  InnsendtDokumentDto("123456789","NAV 11-12.12", false
			, true,"Eksempel","application/pdf", "NAV 11-12.12", 100)

		return SoknadInnsendtDto("100","99","01018012345","TSO", DateTime.now(), Arrays.asList(innsendtDokumentDto))
	}

}
