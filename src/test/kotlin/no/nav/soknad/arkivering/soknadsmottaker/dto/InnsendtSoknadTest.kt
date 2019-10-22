package no.nav.soknad.arkivering.soknadsmottaker.dto

import no.nav.soknad.arkivering.dto.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class InnsendtSoknadTest {

	private final val enkelBilVariantHoveddokumentVariant = opprettHoveddokumentVariant()
	val enkeltBilHovedskjema: InnsendtDokumentDto = innsendtHovedskjemaDokument(enkelBilVariantHoveddokumentVariant)

	@Test
	fun innsendtvedleggErIkkeAvSammeTypeSomHovedskjemaVariant(){
		val enkeltForerkortVedleggVariant = opprettForerkortSomVedleggVariant()
		assertNotEquals("false", enkeltForerkortVedleggVariant.variantformat)
	}

	@Test
	fun innsendtHovedskjemaVariantErAvTypenHovedskjemaVariant() {
		assertEquals("PDF/A", enkelBilVariantHoveddokumentVariant.filtype)
		assertEquals("ARKIV", enkelBilVariantHoveddokumentVariant.variantformat) //TODO dette er fusnkjonalitet som skal flyttes fra Henvendelse til transforamsjon
		assertEquals("true", enkeltBilHovedskjema.erHovedSkjema.toString())
	}

	@Test
	fun `En innsendt soknad ma minst ha et dokument og det er markert som hovedokument`() {
		val hoveddokument = opprettHoveddokumentVariant()
		val soknad = opprettBilInnsendingMedBareSoknadOgKvittering()
		val antallVedlagteDokumenter= soknad.innsendteDokumenter.size
		assertTrue(antallVedlagteDokumenter > 0)
		assertEquals(variantformatBilHovedskjema, hoveddokument.variantformat)
	}
}
