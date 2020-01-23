package no.nav.soknad.arkivering.dto

import java.time.LocalDateTime

data class SoknadMottattDto(val eksternReferanseId: String, val personId: String, val idType: String = "FNR", val tema: String,
														val innsendtDato: LocalDateTime, val mottatteDokumenter: List<MottattDokumentDto>)

data class MottattDokumentDto(var skjemaNummer: String, var erHovedSkjema: Boolean?,
															var tittel: String?, val varianter: List<MottattVariantDto>)

data class MottattVariantDto(val uuid: String, val filNavn: String?, val filtype: String, val variantformat: String)
