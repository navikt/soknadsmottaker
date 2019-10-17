package no.nav.soknad.arkivering.soknadsmottaker.dto

import com.nhaarman.mockitokotlin2.notNull
import no.nav.soknad.arkivering.dto.*
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class InnsendtSoknadTest() {
	val enkelBilVariantHoveddokumentVariant = opprettHoveddokumentVariant()
	val enkeltBilHovedskjema: InnsendtDokumentDto = innsendtHovedskjemaDokument(enkelBilVariantHoveddokumentVariant)

	@Test
	fun innsendtvedleggErIkkeAvSammeTypeSomHovedskjemaVariant(){
		val enkeltForerkortVedleggVariant = opprettForerkortSomVedleggVariant()
		Assertions.assertNotEquals("false", enkeltForerkortVedleggVariant.variantformat.toString())
	}

	@Test
	fun innsendtHovedskjemaVariantErAvTypenHovedskjemaVariant (){
		Assertions.assertEquals("PDF/A", enkelBilVariantHoveddokumentVariant.filtype)
		Assertions.assertEquals("ARKIV", enkelBilVariantHoveddokumentVariant.variantformat) //TODO dette er fusnkjonalitet som skal flyttes fra Henvendelse til transforamsjon
		Assertions.assertEquals("true",enkeltBilHovedskjema.erHovedSkjema.toString() )
	}
	@Test
	fun `En innsendt soknad ma minst ha et dokument og det er markert som hovedokument`(){
		val hoveddokument = opprettHoveddokumentVariant()
		val soknad = opprettBilInnsendingMedBareSoknadOgKvittering()
		val antallVedlagteDokumenter= soknad.innsendteDokumenter.size
		Assertions.assertTrue { antallVedlagteDokumenter > 0 }
		Assertions.assertEquals(variantformatBilHovedskjema, hoveddokument.variantformat)
	}



}
