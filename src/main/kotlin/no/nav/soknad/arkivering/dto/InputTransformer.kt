package no.nav.soknad.arkivering.dto

class InputTransformer (val input: SoknadInnsendtDto) {

	 fun apply () : SoknadMottattDto{
		 return input.toSoknadMottattView()
	}

	fun SoknadInnsendtDto.toSoknadMottattView() = SoknadMottattDto(
	eksternReferanseId= innsendingsId,
	personId = personId,
	tema = tema,
	innsendtDato = innsendtDato,
	mottatteDokumenter = konverterTilMottatteDokumenterList(innsendteDokumenter)
)

private fun InnsendtDokumentDto.toMottattDokumentView() = MottattDokumentDto(
	skjemaNummer = skjemaNummer,
	erHovedSkjema =  erHovedSkjema,
	tittel = tittel,
	varianter =  konverterTilMotattVarianterListe(varianter)
)

private fun InnsendtVariantDto.toMottattVariantView() = MottattVariantDto(
	uuid = uuid,
	filNavn = filNavn,
	filtype = filtype,
	variantformat = variantformat
)
private fun konverterTilMotattVarianterListe(innsendtVariantDto: List<InnsendtVariantDto>): List<MottattVariantDto> {
	return innsendtVariantDto
		.map { f -> f.toMottattVariantView() }
		.toList()
}

private fun konverterTilMottatteDokumenterList(innsendtDto: List<InnsendtDokumentDto>): List<MottattDokumentDto> {
	return innsendtDto
		.map { f -> f.toMottattDokumentView() }
		.toList()
}
}
