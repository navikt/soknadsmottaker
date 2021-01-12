package no.nav.soknad.arkivering.soknadsmottaker.dto

import java.time.LocalDateTime

data class SoknadInnsendtDto(
	val innsendingsId: String, val ettersendelse: Boolean, val personId: String, val tema: String,
	val innsendtDato: LocalDateTime, val innsendteDokumenter: List<InnsendtDokumentDto>
)

data class InnsendtDokumentDto private constructor(val skjemaNummer: String, val erHovedSkjema: Boolean? = false,
															 val tittel: String? = "Ikke spesifisert", val varianter: List<InnsendtVariantDto>) {
	companion object {
		operator fun invoke(skjemaNummer: String, erHovedSkjema: Boolean?,
												tittel: String?, varianter: List<InnsendtVariantDto>
		) = InnsendtDokumentDto(skjemaNummer,
			erHovedSkjema ?: false,
			tittel ?: "Ikke spesifisert",
			varianter
		)
	}
}

data class InnsendtVariantDto private constructor (val uuid: String, val mimeType: String?, val filNavn: String?,
															val filStorrelse: String?, val variantformat: String, val filtype: String) {
	companion object {
		operator fun invoke(uuid: String, mimeType: String?, filNavn: String?, filStorrelse: String?, variantformat: String, filtype: String
		) = InnsendtVariantDto(uuid, mimeType,
			filNavn ?: "Ikke spesifisert",
			filStorrelse ?: "0",
			variantformat, filtype
		)
	}
}
