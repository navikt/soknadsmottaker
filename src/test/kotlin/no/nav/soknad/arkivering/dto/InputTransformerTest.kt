package no.nav.soknad.arkivering.dto

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class InputTransformerTest {
	private val innsendtSoknad = opprettBilInnsendingMedBareSoknadOgKvittering()

  @Test
  fun sjekkAtInnsendtSoknadTransformeresTilMottattSoknadOgSjekkInformasjontransformering() {
		val transformertSoknad = transformereSoknad()

		assertEquals(innsendtSoknad.innsendingsId, transformertSoknad.eksternReferanseId)
		assertEquals(innsendtSoknad.personId, transformertSoknad.personId)
		assertEquals(innsendtSoknad.tema, transformertSoknad.tema)
		assertEquals(innsendtSoknad.innsendtDato, transformertSoknad.innsendtDato)
		assertEquals(innsendtSoknad.innsendteDokumenter.size, transformertSoknad.mottatteDokumenter.size)
	}

	@Test
	fun `innsendt dokumentformat blir transformert til mottatt dokument`() {
		val transformertSoknad = transformereSoknad()
		val motattHoveddokument = transformertSoknad.mottatteDokumenter.find { it.erHovedSkjema == true }

		assertEquals(skjemanummerBil, motattHoveddokument?.skjemaNummer)
		assertEquals(true, motattHoveddokument?.erHovedSkjema)
		assertEquals(tittelBil, motattHoveddokument?.tittel)
	}

	@Test
	fun `innsendte variant for hoveddokument blir transformert til mottatt variant for hoveddokument`() {

		val forventetUuidHoveddokument = uuidBil

		val transformertSoknad = transformereSoknad()
		val mottattHoveddokument = transformertSoknad.mottatteDokumenter.find { it.erHovedSkjema == true }
		val variantFormatForHoveddokument = mottattHoveddokument?.varianter?.find { it.uuid == forventetUuidHoveddokument }

		assertEquals(forventetUuidHoveddokument, variantFormatForHoveddokument?.uuid)
		assertEquals(filNavnBil, variantFormatForHoveddokument?.filNavn)
		assertEquals(variantformatBilHovedskjema, variantFormatForHoveddokument?.variantformat)
		assertEquals(filtypeBilHoveskjema, variantFormatForHoveddokument?.filtype)
	}

	@Test
	fun `insendt vedleggsvariant kvittering transformeres riktig`() {

		val transformertSoknad = transformereSoknad()

		val forventetUuidKvittering = uuidBilKvittering
		val mottattKvittering = transformertSoknad.mottatteDokumenter.find { it.erHovedSkjema == false }
		val variantFormatForKvittering = mottattKvittering?.varianter?.find { it.uuid == forventetUuidKvittering }

		assertEquals(forventetUuidKvittering, variantFormatForKvittering?.uuid)
		assertEquals(filnavnKvitteering, variantFormatForKvittering?.filNavn)
		assertEquals(variantformatBilKvittering, variantFormatForKvittering?.variantformat)
		assertEquals(filtypeBilKvittering, variantFormatForKvittering?.filtype)
	}

	private fun transformereSoknad() = InputTransformer(innsendtSoknad).apply()
}
