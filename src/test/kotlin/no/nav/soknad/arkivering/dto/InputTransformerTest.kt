package no.nav.soknad.arkivering.dto

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals

class InputTransformerTest {
	val enkelBilSoknad = opprettBilInnsendingMedBareSoknadOgKvittering()
	val enkelBilVariantHoveddokumentVariant = opprettHoveddokumentVariant()
	val enkeltBilHovedskjema: InnsendtDokumentDto = innsendtHovedskjemaDokument(enkelBilVariantHoveddokumentVariant)
	val enkeltForerkortVedleggVariant = opprettForerkortSomVedleggVariant()
	val enkeltForerkortVedleggDokumentVariant = opprettForerkortSomVedleggVariant()

	@Test
	fun apply() {
		enkelBilSoknad
	}

	@Test
	fun toSoknadMottattView() {

	}

	@Test
	fun getInput() {
	}

	@Test
	fun innsendtHovedskjemaVariantErAvTypenHovedskjemaVariant() {
		assertEquals("PDF/A", enkelBilVariantHoveddokumentVariant.filtype)
		assertEquals("ARKIV", enkelBilVariantHoveddokumentVariant.variantformat)
		assertEquals("true", enkeltBilHovedskjema.erHovedSkjema.toString())
	}

	@Test
	fun mottattBilSoknad() {
		val ettersendelseSkalVareFalse = false
		assertEquals(enkelBilSoknad.ettersendelse, ettersendelseSkalVareFalse)
		assertEquals(enkelBilSoknad.innsendteDokumenter.size, 2)
	}
}
