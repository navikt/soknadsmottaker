package no.nav.soknad.arkivering.soknadsmottaker.utils

import no.nav.soknad.arkivering.soknadsmottaker.model.DocumentData
import no.nav.soknad.arkivering.soknadsmottaker.model.Soknad
import no.nav.soknad.arkivering.soknadsmottaker.model.Varianter

fun createSoknad() = Soknad(
	"17e10f63-443c-4aba-829b-d598c3a74248",
	false,
	"01234567891",
	"BIL",
	createDocuments()
)

fun createDocuments(erHovedskjema: Boolean = true, variants: List<Varianter> = createVariants()) = listOf(DocumentData(
	"NAV 10-07.40",
	erHovedskjema,
	"Søknad om stønad til anskaffelse av motorkjøretøy",
	variants
))

fun createVariants() = listOf(createVariant())

fun createVariant(mediaType: String = "application/pdf") = Varianter(
	"e7179251-635e-493a-948c-749a39eedacc",
	mediaType,
	"innsending.pdf",
	"PDFA"
)
