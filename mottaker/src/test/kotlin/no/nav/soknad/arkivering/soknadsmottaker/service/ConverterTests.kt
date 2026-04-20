package no.nav.soknad.arkivering.soknadsmottaker.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.soknad.arkivering.avroschemas.Soknadstyper
import no.nav.soknad.arkivering.soknadsmottaker.model.AvsenderDto
import no.nav.soknad.arkivering.soknadsmottaker.model.BrukerDto
import no.nav.soknad.arkivering.soknadsmottaker.model.InnsendingTopicMsg
import no.nav.soknad.arkivering.soknadsmottaker.util.mapTilInnsendingTopicMsg
import no.nav.soknad.arkivering.soknadsmottaker.utils.createDocuments
import no.nav.soknad.arkivering.soknadsmottaker.utils.createInnsending
import no.nav.soknad.arkivering.soknadsmottaker.utils.createSoknad
import no.nav.soknad.arkivering.soknadsmottaker.utils.createVariant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

class ConverterTests {
	private val soknad = createSoknad()

	@Test
	fun `Can convert correctly`() {
		val startTime = OffsetDateTime.now().toEpochSecond()

		val result = convert(soknad)

		assertEquals(soknad.innsendingId, result.behandlingsid)
		assertEquals(soknad.personId, result.fodselsnummer)
		assertEquals(soknad.tema, result.arkivtema)
		val endTime = OffsetDateTime.now().toEpochSecond()
		assertTrue(result.innsendtDato >= startTime)
		assertTrue(result.innsendtDato <= endTime)
		val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
		val innsendtDatoStreng = Instant.ofEpochSecond(result.innsendtDato)
			.atZone(ZoneId.of("UTC"))
			.format(formatter)

		assertEquals(1, result.mottatteDokumenter.size)

		assertEquals(soknad.dokumenter[0].skjemanummer, result.mottatteDokumenter[0].skjemanummer)
		assertEquals(soknad.dokumenter[0].erHovedskjema, result.mottatteDokumenter[0].erHovedskjema)
		assertEquals(soknad.dokumenter[0].tittel, result.mottatteDokumenter[0].tittel)
		assertEquals(1, result.mottatteDokumenter[0].mottatteVarianter.size)

		assertEquals(soknad.dokumenter[0].varianter[0].id, result.mottatteDokumenter[0].mottatteVarianter[0].uuid)
		assertEquals(soknad.dokumenter[0].varianter[0].filnavn, result.mottatteDokumenter[0].mottatteVarianter[0].filnavn)
		assertEquals(soknad.dokumenter[0].varianter[0].filtype, result.mottatteDokumenter[0].mottatteVarianter[0].filtype)
		assertEquals("ARKIV", result.mottatteDokumenter[0].mottatteVarianter[0].variantformat)
	}

	@Test
	fun `Can convert Soknadstyper`() {
		val result0 = convert(soknad.copy(erEttersendelse = false))
		assertEquals(Soknadstyper.SOKNAD, result0.soknadstype)

		val result1 = convert(soknad.copy(erEttersendelse = true))
		assertEquals(Soknadstyper.ETTERSENDING, result1.soknadstype)
	}

	@Test
	fun `Can convert Variantformat`() {
		val result0 = convert(soknad.copy(dokumenter = createDocuments(listOf(createVariant("application/pdf")))))
		assertTrue(soknad.dokumenter[0].erHovedskjema)
		assertEquals("ARKIV", result0.mottatteDokumenter[0].mottatteVarianter[0].variantformat)

		val result1 = convert(soknad.copy(dokumenter = createDocuments(listOf(createVariant("application/pdf-fullversjon")))))
		assertEquals("FULLVERSJON", result1.mottatteDokumenter[0].mottatteVarianter[0].variantformat)

		val result2 = convert(soknad.copy(dokumenter = createDocuments(listOf(createVariant("application/json")))))
		assertEquals("ORIGINAL", result2.mottatteDokumenter[0].mottatteVarianter[0].variantformat)

		val result3 = convert(soknad.copy(dokumenter = createDocuments(listOf(createVariant("application/xml")))))
		assertEquals("ORIGINAL", result3.mottatteDokumenter[0].mottatteVarianter[0].variantformat)
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

	fun createUtcPreservingMapper(): com.fasterxml.jackson.databind.ObjectMapper {
		val mapper = jacksonObjectMapper()
		mapper.registerModule(JavaTimeModule())
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
		mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
		return mapper
	}

	fun deserializeMsg(msgString: String): InnsendingTopicMsg {
		return createUtcPreservingMapper()
			.readValue(msgString, InnsendingTopicMsg::class.java)
	}

}
