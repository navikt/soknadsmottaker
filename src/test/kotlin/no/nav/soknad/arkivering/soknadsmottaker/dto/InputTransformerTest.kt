package no.nav.soknad.arkivering.soknadsmottaker.dto

import no.nav.soknad.soknadarkivering.avroschemas.Soknadstyper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.ZoneOffset

class InputTransformerTest {
	private val innsendtSoknad = opprettBilInnsendingMedBareSoknadOgKvittering()

	@Test
	fun `Sjekk at innsendt soknad transformeres til mottatt soknad og sjekk informasjontransformering`() {
		val transformertSoknad = transformereSoknad()

		assertEquals(innsendtSoknad.innsendingsId, transformertSoknad.getBehandlingsid())
		assertEquals(innsendtSoknad.personId, transformertSoknad.getFodselsnummer())
		assertEquals(innsendtSoknad.tema, transformertSoknad.getArkivtema())
		assertEquals(innsendtSoknad.innsendtDato.toEpochSecond(ZoneOffset.UTC), transformertSoknad.getInnsendtDato())
		assertEquals(innsendtSoknad.innsendteDokumenter.size, transformertSoknad.getMottatteDokumenter().size)
	}

	@Test
	fun `Innsendt dokumentformat blir transformert til mottatt dokument`() {
		val transformertSoknad = transformereSoknad()
		val motattHoveddokument = transformertSoknad.getMottatteDokumenter().find { it.getErHovedskjema() }

		assertEquals(skjemanummerBil, motattHoveddokument?.getSkjemanummer())
		assertEquals(true, motattHoveddokument?.getErHovedskjema())
		assertEquals(tittelBil, motattHoveddokument?.getTittel())
	}

	@Test
	fun `Innsendte variant for hoveddokument blir transformert til mottatt variant for hoveddokument`() {

		val forventetUuidHoveddokument = uuidBil

		val transformertSoknad = transformereSoknad()
		val mottattHoveddokument = transformertSoknad.getMottatteDokumenter().find { it.getErHovedskjema() }
		val variantFormatForHoveddokument = mottattHoveddokument?.getMottatteVarianter()?.find { it.getUuid() == forventetUuidHoveddokument }

		assertEquals(forventetUuidHoveddokument, variantFormatForHoveddokument?.getUuid())
		assertEquals(filNavnBil, variantFormatForHoveddokument?.getFilnavn())
		assertEquals(variantformatBilHovedskjema, variantFormatForHoveddokument?.getVariantformat())
		assertEquals(filtypeBilHoveskjema, variantFormatForHoveddokument?.getFiltype())
	}

	@Test
	fun `Innsendt vedleggsvariant kvittering transformeres riktig`() {

		val transformertSoknad = transformereSoknad()

		val forventetUuidKvittering = uuidBilKvittering
		val mottattKvittering = transformertSoknad.getMottatteDokumenter().find { !it.getErHovedskjema() }
		val variantFormatForKvittering = mottattKvittering?.getMottatteVarianter()?.find { it.getUuid() == forventetUuidKvittering }

		assertEquals(forventetUuidKvittering, variantFormatForKvittering?.getUuid())
		assertEquals(filnavnKvitteering, variantFormatForKvittering?.getFilnavn())
		assertEquals(variantformatBilKvittering, variantFormatForKvittering?.getVariantformat())
		assertEquals(filtypeBilKvittering, variantFormatForKvittering?.getFiltype())
	}

	@Test
	fun `Ettersendelse=true gives the right Soknadstype`() {
		val transformertSoknad = transformereSoknad(innsendtSoknad.copy(ettersendelse = true))

		assertEquals(Soknadstyper.ETTERSENDING, transformertSoknad.getSoknadstype())
	}

	@Test
	fun `Ettersendelse=false gives the right Soknadstype`() {
		val transformertSoknad = transformereSoknad(innsendtSoknad.copy(ettersendelse = false))

		assertEquals(Soknadstyper.SOKNAD, transformertSoknad.getSoknadstype())
	}

	private fun transformereSoknad(soknad: SoknadInnsendtDto = innsendtSoknad) = InputTransformer(soknad).apply()
}
