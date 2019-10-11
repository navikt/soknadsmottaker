package no.nav.soknad.arkivering.dto

import org.joda.time.DateTime

data class SoknadInnsendtDto (var henvendelsesId: String, var ettersendelsesId: String?, var personId: String, var tema: String
															, var innsendtDato: DateTime, var innsendteDokumenter: List<InnsendtDokumentDto>)

data class InnsendtDokumentDto(var uuid: String, var skjemaNummer: String, var erAlternativRepresentasjon: Boolean, var erHovedSkjema: Boolean,
															 var tittel: String?, var mimeType: String?, var filNavn: String?, var filStorrelse: Int?)
