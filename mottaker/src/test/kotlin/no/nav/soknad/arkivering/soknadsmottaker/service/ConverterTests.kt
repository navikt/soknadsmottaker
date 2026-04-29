package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.soknad.arkivering.soknadsmottaker.model.AvsenderDto
import no.nav.soknad.arkivering.soknadsmottaker.model.BrukerDto
import no.nav.soknad.arkivering.soknadsmottaker.util.mapTilInnsendingTopicMsg
import no.nav.soknad.arkivering.soknadsmottaker.utils.createInnsending
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.UUID

class ConverterTests {
	private val soknad = createInnsending(
		brukerDto = BrukerDto("12345678901", BrukerDto.IdType.FNR),
		avsenderDto = AvsenderDto(
			id = "12345678901",
			idType = AvsenderDto.IdType.FNR,
			navn = null
		), kanal = "NAV_NO", tema = "BIL", innsendtDato = OffsetDateTime.now()
	)


	@Test
	fun `Can convert correctly`() {
		val result = mapTilInnsendingTopicMsg(soknad, true)

		assertEquals(soknad.innsendingsId, result.innsendingsId)
		assertEquals(soknad.brukerDto?.id, result.brukerDto?.id)
		assertEquals(soknad.brukerDto?.idType.toString(), result.brukerDto?.idType.toString())
		assertEquals(soknad.avsenderDto.id, result.avsenderDto.id)
		assertEquals(soknad.avsenderDto.idType.toString(), result.avsenderDto.idType.toString())
		assertEquals(soknad.tema, result.arkivtema)
		assertEquals(soknad.innsendtDato, result.innsendtDato)
		assertEquals(soknad.ettersendelseTilId, result.ettersendelseTilId)
		assertEquals(soknad.kanal, result.kanal)
		assertEquals(soknad.skjemanr, result.skjemanr)
		assertEquals(soknad.tittel, result.tittel)

		assertEquals(3, result.dokumenter.size)

		assertEquals(soknad.dokumenter[0].skjemanummer, result.dokumenter[0].skjemanummer)
		assertEquals(soknad.dokumenter[0].erHovedskjema, result.dokumenter[0].erHovedskjema)
		assertEquals(soknad.dokumenter[0].tittel, result.dokumenter[0].tittel)
		assertEquals(2, result.dokumenter[0].varianter.size)

		assertEquals(soknad.dokumenter[0].varianter[0].uuid, result.dokumenter[0].varianter[0].uuid)
		assertEquals(soknad.dokumenter[0].varianter[0].filnavn, result.dokumenter[0].varianter[0].filnavn)
		assertEquals(soknad.dokumenter[0].varianter[0].filtype, result.dokumenter[0].varianter[0].filtype)
		assertEquals("ARKIV", result.dokumenter[0].varianter[0].variantFormat)
	}

	@Test
	fun `Can convert innsending message`() {
		val soknad = createInnsending(
			brukerDto = BrukerDto("01234567891", BrukerDto.IdType.FNR),
			avsenderDto = AvsenderDto(
				id = "01234567891",
				idType = AvsenderDto.IdType.FNR,
				navn = null
			), kanal = "NAV_NO_UINNLOGGET", tema = "HJE",
			innsendtDato = null
		)
		val startTime = OffsetDateTime.now()
		val convertedSoknad = mapTilInnsendingTopicMsg(soknad, false)

		assertEquals(soknad.innsendingsId, convertedSoknad.innsendingsId)
		assertEquals(soknad.tema, convertedSoknad.arkivtema)
		assertEquals(soknad.kanal, convertedSoknad.kanal)
		assertEquals(soknad.brukerDto?.id, convertedSoknad.brukerDto?.id)
		assertEquals(soknad.avsenderDto.id, convertedSoknad.avsenderDto.id)
		assertEquals(soknad.avsenderDto.idType, convertedSoknad.avsenderDto.idType)
		assertEquals(soknad.dokumenter.size, convertedSoknad.dokumenter.size)
		assertEquals(soknad.dokumenter.filter{it.erHovedskjema}.first().varianter.map{it.variantFormat},
			convertedSoknad.dokumenter.filter{it.erHovedskjema}.first().varianter.map{it.variantFormat}, "Should send correct variantFormats")
		assertEquals(soknad.dokumenter.map{it.varianter.map{variant-> variant.uuid}}.flatten(),
			convertedSoknad.dokumenter.map{it.varianter.map{variant-> variant.uuid}}.flatten(), "Should send correct uuids")
		val endTime = OffsetDateTime.now()
		assertTrue(startTime.isBefore(convertedSoknad.innsendtDato) && convertedSoknad.innsendtDato.isBefore(endTime))
	}

	@Test
	fun `Can convert innsending message with innsendtDato`() {
		val soknad = createInnsending(
			brukerDto = BrukerDto("01234567891", BrukerDto.IdType.FNR),
			avsenderDto = AvsenderDto(
				id = "01234567891",
				idType = AvsenderDto.IdType.FNR,
				navn = null
			), kanal = "NAV_NO_UINNLOGGET", tema = "HJE",
			innsendtDato = OffsetDateTime.now(),
			ettersendelseTilId = UUID.randomUUID().toString()
		)
		val convertedSoknad = mapTilInnsendingTopicMsg(soknad, false)

		assertEquals(soknad.innsendingsId, convertedSoknad.innsendingsId)
		assertEquals(soknad.tema, convertedSoknad.arkivtema)
		assertEquals(soknad.kanal, convertedSoknad.kanal)
		assertEquals(soknad.brukerDto?.id, convertedSoknad.brukerDto?.id)
		assertEquals(soknad.avsenderDto.id, convertedSoknad.avsenderDto.id)
		assertEquals(soknad.avsenderDto.idType, convertedSoknad.avsenderDto.idType)
		assertEquals(soknad.dokumenter.size, convertedSoknad.dokumenter.size)
		assertEquals(soknad.dokumenter.filter{it.erHovedskjema}.first().varianter.map{it.variantFormat},
			convertedSoknad.dokumenter.filter{it.erHovedskjema}.first().varianter.map{it.variantFormat}, "Should send correct variantFormats")
		assertEquals(soknad.dokumenter.map{it.varianter.map{variant-> variant.uuid}}.flatten(),
			convertedSoknad.dokumenter.map{it.varianter.map{variant-> variant.uuid}}.flatten(), "Should send correct uuids")
		assertEquals(soknad.innsendtDato, convertedSoknad.innsendtDato, "Should preserve innsendtDato")
		assertEquals(soknad.ettersendelseTilId, convertedSoknad.ettersendelseTilId, "Should preserve ettersendelseTilId")
	}

}
