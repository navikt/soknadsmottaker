package no.nav.soknad.archiving.archiverproducer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ArchiverProducerApplication

fun main(args: Array<String>) {
	runApplication<ArchiverProducerApplication>(*args)
}
