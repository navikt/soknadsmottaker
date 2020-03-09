package no.nav.soknad.arkivering.soknadsmottaker.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.ZoneOffset

class InputTransformerTest {
	private val innsendtSoknad = opprettBilInnsendingMedBareSoknadOgKvittering()

	@Test
	fun sjekkAtInnsendtSoknadTransformeresTilMottattSoknadOgSjekkInformasjontransformering() {
		val transformertSoknad = transformereSoknad()

		assertEquals(innsendtSoknad.innsendingsId, transformertSoknad.getBehandlingsid())
		assertEquals(innsendtSoknad.personId, transformertSoknad.getFodselsnummer())
		assertEquals(innsendtSoknad.tema, transformertSoknad.getArkivtema())
		assertEquals(innsendtSoknad.innsendtDato.toEpochSecond(ZoneOffset.UTC), transformertSoknad.getHenvendelseInnsendtDato())
		assertEquals(innsendtSoknad.innsendteDokumenter.size, transformertSoknad.getMottatteDokumenter().size)
	}

	@Test
	fun `innsendt dokumentformat blir transformert til mottatt dokument`() {
		val transformertSoknad = transformereSoknad()
		val motattHoveddokument = transformertSoknad.getMottatteDokumenter().find { it.getErHovedskjema() == true }

		assertEquals(skjemanummerBil, motattHoveddokument?.getSkjemanummer())
		assertEquals(true, motattHoveddokument?.getErHovedskjema())
		assertEquals(tittelBil, motattHoveddokument?.getTittel())
	}

	@Test
	fun `innsendte variant for hoveddokument blir transformert til mottatt variant for hoveddokument`() {

		val forventetUuidHoveddokument = uuidBil

		val transformertSoknad = transformereSoknad()
		val mottattHoveddokument = transformertSoknad.getMottatteDokumenter().find { it.getErHovedskjema() == true }
		val variantFormatForHoveddokument = mottattHoveddokument?.getMottatteVarianter()?.find { it.getUuid() == forventetUuidHoveddokument }

		assertEquals(forventetUuidHoveddokument, variantFormatForHoveddokument?.getUuid())
		assertEquals(filNavnBil, variantFormatForHoveddokument?.getFilnavn())
		assertEquals(variantformatBilHovedskjema, variantFormatForHoveddokument?.getVariantformat())
		assertEquals(filtypeBilHoveskjema, variantFormatForHoveddokument?.getFiltype())
	}

	@Test
	fun `insendt vedleggsvariant kvittering transformeres riktig`() {

		val transformertSoknad = transformereSoknad()

		val forventetUuidKvittering = uuidBilKvittering
		val mottattKvittering = transformertSoknad.getMottatteDokumenter().find { it.getErHovedskjema() == false }
		val variantFormatForKvittering = mottattKvittering?.getMottatteVarianter()?.find { it.getUuid() == forventetUuidKvittering }

		assertEquals(forventetUuidKvittering, variantFormatForKvittering?.getUuid())
		assertEquals(filnavnKvitteering, variantFormatForKvittering?.getFilnavn())
		assertEquals(variantformatBilKvittering, variantFormatForKvittering?.getVariantformat())
		assertEquals(filtypeBilKvittering, variantFormatForKvittering?.getFiltype())
	}

	private fun transformereSoknad() = InputTransformer(innsendtSoknad).apply()
}
