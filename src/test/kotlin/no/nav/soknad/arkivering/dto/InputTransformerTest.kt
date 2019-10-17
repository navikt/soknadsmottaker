package no.nav.soknad.arkivering.dto

import org.junit.jupiter.api.Test
import no.nav.soknad.arkivering.dto.DtoObjectMother
import org.junit.jupiter.api.Assertions

internal class InputTransformerTest {
	val enkelBilSoknad = opprettBilInnsendingMedBareSoknadOgKvittering()

    @Test
    fun apply() {

    }

    @Test
    fun toSoknadMottattView() {

		}

    @Test
    fun getInput() {
    }
	@Test
	fun mottattBilSoknad () {
		val ettersendelseSkalVareFalse = false
		Assertions.assertEquals(enkelBilSoknad.ettersendelse, ettersendelseSkalVareFalse)
		Assertions.assertEquals(enkelBilSoknad.innsendteDokumenter.size, 2)

	}
}
