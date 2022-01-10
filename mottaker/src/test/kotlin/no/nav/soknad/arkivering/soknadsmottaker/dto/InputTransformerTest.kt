package no.nav.soknad.arkivering.soknadsmottaker.dto

import no.nav.soknad.arkivering.avroschemas.Soknadstyper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.ZoneOffset

class InputTransformerTest {
	private val innsendtSoknad = opprettBilInnsendingMedBareSoknadOgKvittering()

	@Test
	fun `Sjekk at innsendt soknad transformeres til mottatt soknad og sjekk informasjontransformering`() {
		val transformertSoknad = transformereSoknad()

		assertEquals(innsendtSoknad.innsendingsId, transformertSoknad.behandlingsid)
		assertEquals(innsendtSoknad.personId, transformertSoknad.fodselsnummer)
		assertEquals(innsendtSoknad.tema, transformertSoknad.arkivtema)
		assertEquals(innsendtSoknad.innsendtDato.toEpochSecond(ZoneOffset.UTC), transformertSoknad.innsendtDato)
		assertEquals(innsendtSoknad.innsendteDokumenter.size, transformertSoknad.mottatteDokumenter.size)
	}

	@Test
	fun `Innsendt dokumentformat blir transformert til mottatt dokument`() {
		val transformertSoknad = transformereSoknad()
		val motattHoveddokument = transformertSoknad.mottatteDokumenter.find { it.erHovedskjema }

		assertEquals(skjemanummerBil, motattHoveddokument?.skjemanummer)
		assertEquals(true, motattHoveddokument?.erHovedskjema)
		assertEquals(tittelBil, motattHoveddokument?.tittel)
	}

	@Test
	fun `Innsendte variant for hoveddokument blir transformert til mottatt variant for hoveddokument`() {

		val forventetUuidHoveddokument = uuidBil

		val transformertSoknad = transformereSoknad()
		val mottattHoveddokument = transformertSoknad.mottatteDokumenter.find { it.erHovedskjema }
		val variantFormatForHoveddokument =
			mottattHoveddokument?.mottatteVarianter?.find { it.uuid == forventetUuidHoveddokument }

		assertEquals(forventetUuidHoveddokument, variantFormatForHoveddokument?.uuid)
		assertEquals(filNavnBil, variantFormatForHoveddokument?.filnavn)
		assertEquals(variantformatBilHovedskjema, variantFormatForHoveddokument?.variantformat)
		assertEquals(filtypeBilHoveskjema, variantFormatForHoveddokument?.filtype)
	}

	@Test
	fun `Innsendt vedleggsvariant kvittering transformeres riktig`() {

		val transformertSoknad = transformereSoknad()

		val forventetUuidKvittering = uuidBilKvittering
		val mottattKvittering = transformertSoknad.mottatteDokumenter.find { !it.erHovedskjema }
		val variantFormatForKvittering =
			mottattKvittering?.mottatteVarianter?.find { it.uuid == forventetUuidKvittering }

		assertEquals(forventetUuidKvittering, variantFormatForKvittering?.uuid)
		assertEquals(filnavnKvitteering, variantFormatForKvittering?.filnavn)
		assertEquals(variantformatBilKvittering, variantFormatForKvittering?.variantformat)
		assertEquals(filtypeBilKvittering, variantFormatForKvittering?.filtype)
	}

	@Test
	fun `Ettersendelse=true gives the right Soknadstype`() {
		val transformertSoknad = transformereSoknad(innsendtSoknad.copy(ettersendelse = true))

		assertEquals(Soknadstyper.ETTERSENDING, transformertSoknad.soknadstype)
	}

	@Test
	fun `Ettersendelse=false gives the right Soknadstype`() {
		val transformertSoknad = transformereSoknad(innsendtSoknad.copy(ettersendelse = false))

		assertEquals(Soknadstyper.SOKNAD, transformertSoknad.soknadstype)
	}

	private fun transformereSoknad(soknad: SoknadInnsendtDto = innsendtSoknad) = InputTransformer(soknad).apply()
}
