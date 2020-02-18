package no.nav.soknad.arkivering.soknadsmottaker.service

import com.natpryce.konfig.Key
import com.natpryce.konfig.stringType
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import no.nav.soknad.arkivering.dto.SoknadMottattDto
import no.nav.soknad.arkivering.soknadsmottaker.config.AppConfiguration
import no.nav.soknad.arkivering.soknadsmottaker.config.appConfig
import org.junit.jupiter.api.Assertions
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

	@Test
	fun `Sjekker innlesning av miljovariable`() {
		val config = AppConfiguration()
		println(config)
		Assertions.assertEquals(config.kafkaConfig.username, "srvsoknadsmottaker")
		Assertions.assertEquals(config.kafkaConfig.topic, "privat-soknadInnsendt-sendsoknad-v1-default")
	}

}
