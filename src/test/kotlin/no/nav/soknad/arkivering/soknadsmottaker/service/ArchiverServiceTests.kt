package no.nav.soknad.arkivering.soknadsmottaker.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import no.nav.soknad.arkivering.soknadsmottaker.config.AppConfiguration
import org.junit.jupiter.api.Assertions
import no.nav.soknad.arkivering.dto.opprettBilInnsendingMedBareSoknadOgKvittering
import no.nav.soknad.arkivering.soknadsmottaker.dto.opprettBilInnsendingMedBareSoknadOgKvittering
import org.junit.jupiter.api.Test

class ArchiverServiceTests {

	private val kafkaSender = mock<KafkaSender> { }
	private val archiverService = ArchiverService(kafkaSender)

	@Test
	fun `Kaller Kafka sender`() {
		archiverService.archive(opprettBilInnsendingMedBareSoknadOgKvittering())

		verify(kafkaSender, times(1)).publish(any(), any(), any())
	}


	@Test
	fun `Sjekker innlesning av miljovariable`() {
		val config = AppConfiguration()
		println(config)
		Assertions.assertEquals(config.kafkaConfig.username, "srvsoknadsmottaker")
		Assertions.assertEquals(config.kafkaConfig.topic, "privat-soknadInnsendt-sendsoknad-v1-default")
	}

}
