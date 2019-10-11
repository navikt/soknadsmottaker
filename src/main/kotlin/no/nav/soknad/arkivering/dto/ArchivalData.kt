package no.nav.soknad.arkivering.dto

import org.joda.time.DateTime

data class SoknadMottattDto (var henvendelsesId: String, var ettersendelsesId: String?, var personId: String, var tema: String
														 , var innsendtDato: DateTime, var mottatteDokumenter: List<MottattDokumentDto>)

data class MottattDokumentDto(var uuid: String, var skjemaNummer: String, var erAlternativRepresentasjon: Boolean, var erHovedSkjema: Boolean,
															 var tittel: String?, var mimeType: String?, var filNavn: String?, var filStorrelse: Int?)
