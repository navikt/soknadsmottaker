package no.nav.soknad.arkivering.dto

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions

internal class InputTransformerTest {
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
	fun innsendtHovedskjemaVariantErAvTypenHovedskjemaVariant (){
		Assertions.assertEquals("PDF/A", enkelBilVariantHoveddokumentVariant.filtype)
		Assertions.assertEquals("ARKIV", enkelBilVariantHoveddokumentVariant.variantformat)
		Assertions.assertEquals("true",enkeltBilHovedskjema.erHovedSkjema.toString() )
	}

	@Test
	fun mottattBilSoknad () {
		val ettersendelseSkalVareFalse = false
		Assertions.assertEquals(enkelBilSoknad.ettersendelse, ettersendelseSkalVareFalse)
		Assertions.assertEquals(enkelBilSoknad.innsendteDokumenter.size, 2)
	}
}
