package no.nav.soknad.arkivering.soknadsmottaker.service

import no.nav.soknad.arkivering.avroschemas.Soknadstyper
import no.nav.soknad.arkivering.soknadsmottaker.utils.createDocuments
import no.nav.soknad.arkivering.soknadsmottaker.utils.createSoknad
import no.nav.soknad.arkivering.soknadsmottaker.utils.createVariant
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class ConverterTests {
	private val soknad = createSoknad()

	@Test
	fun `Can convert correctly`() {
		val startTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)

		val result = convert(soknad)

		assertEquals(soknad.innsendingId, result.behandlingsid)
		assertEquals(soknad.personId, result.fodselsnummer)
		assertEquals(soknad.tema, result.arkivtema)
		val endTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
		assertTrue(result.innsendtDato >= startTime)
		assertTrue(result.innsendtDato <= endTime)
		assertEquals(Soknadstyper.SOKNAD, result.soknadstype)
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
		val result0 = convert(soknad)
		assertTrue(soknad.dokumenter[0].erHovedskjema)
		assertEquals("ARKIV", result0.mottatteDokumenter[0].mottatteVarianter[0].variantformat)

		val result1 = convert(soknad.copy(dokumenter = createDocuments(false, listOf(createVariant()))))
		assertEquals("FULLVERSJON", result1.mottatteDokumenter[0].mottatteVarianter[0].variantformat)

		val result2 = convert(soknad.copy(dokumenter = createDocuments(false, listOf(createVariant("application/json")))))
		assertEquals("ORIGINAL", result2.mottatteDokumenter[0].mottatteVarianter[0].variantformat)

		val result3 = convert(soknad.copy(dokumenter = createDocuments(false, listOf(createVariant("application/xml")))))
		assertEquals("ORIGINAL", result3.mottatteDokumenter[0].mottatteVarianter[0].variantformat)
	}
}
