package no.nav.soknad.arkivering.dto

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions

internal class InputTransformerTest {
	private val innsendtSoknad = opprettBilInnsendingMedBareSoknadOgKvittering()
	private val enkelBilVariantHoveddokumentVariant = opprettHoveddokumentVariant()
	private val enkeltBilHovedskjema: InnsendtDokumentDto = innsendtHovedskjemaDokument(enkelBilVariantHoveddokumentVariant)
	private val transformertSoknad = InputTransformer(innsendtSoknad).apply()
	private val motattHoveddokument = transformertSoknad.mottatteDokumenter.find { it.erHovedSkjema == true }
	private val motattHoveddokumentVarianer = motattHoveddokument?.varianter
	private val varianFormatErARKIV = motattHoveddokumentVarianer?.find { it.uuid == uuidBil }

  @Test
  fun sjekkAtInnsendtSoknadTransformeresTilMottattSoknadOgSjekkInformasjontransformering() {

		Assertions.assertEquals(innsendtSoknad.innsendingsId, transformertSoknad.eksternReferanseId)
		Assertions.assertEquals(innsendtSoknad.personId, transformertSoknad.personId)
		Assertions.assertEquals(innsendtSoknad.tema, transformertSoknad.tema)
		Assertions.assertEquals(innsendtSoknad.innsendtDato, transformertSoknad.innsendtDato)
		Assertions.assertEquals(innsendtSoknad.innsendteDokumenter.size, transformertSoknad.mottatteDokumenter.size)

	}

	@Test
	fun `innsendt dokumentformat blir transformert til mottatt dokument`() {

		Assertions.assertEquals(skjemanummerBil, motattHoveddokument?.skjemaNummer)
		Assertions.assertEquals("true", motattHoveddokument?.erHovedSkjema.toString())
		Assertions.assertEquals(tittelBil, motattHoveddokument?.tittel)

	}

	@Test
	fun `innsendt variant blir transformert til mottat variant`() {
		val hoveddokumentVariant = varianFormatErARKIV
		val forventetUuid = uuidBil

		Assertions.assertEquals(forventetUuid, hoveddokumentVariant?.uuid)
		Assertions.assertEquals(filNavnBil, hoveddokumentVariant?.filNavn)
		Assertions.assertEquals(variantformatBilHovedskjema, hoveddokumentVariant?.variantformat)

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
		Assertions.assertEquals(innsendtSoknad.ettersendelse, ettersendelseSkalVareFalse)
		Assertions.assertEquals(innsendtSoknad.innsendteDokumenter.size, 2)
	}
}
