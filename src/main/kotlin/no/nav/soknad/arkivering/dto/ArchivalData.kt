package no.nav.soknad.arkivering.dto

import org.joda.time.DateTime


data class SoknadMottattDto (val eksternReferanseId: String, val personId: String,val idType: String = "FNR", val tema: String
														 , val innsendtDato: DateTime, val mottatteDokumenter: List<MottattDokumentDto>)

data class MottattDokumentDto(var skjemaNummer: String, var erHovedSkjema: Boolean?,
															var tittel: String?, val varianter: List<MottattVariantDto>)

data class MottattVariantDto(val uuid: String, val filNavn: String?, val filtype: String, val variantformat: String)
