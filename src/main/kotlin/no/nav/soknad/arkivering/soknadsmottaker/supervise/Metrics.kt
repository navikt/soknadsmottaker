package no.nav.soknad.arkivering.soknadsmottaker.supervise

import io.prometheus.client.Counter

internal object Metrics {

	private const val SOKNAD_NAMESPACE = "soknadinnsending"
	private const val SUB_SYSTEM = "soknadsMottaker"
	private const val NAME = "innsendte"
	private const val HELP = "Number of applications and documents received and published to Kafka stream"
	private const val LABEL_NAME = "tema"
	private const val ERROR = "feil"
	private const val HELP_ERROR = "Number of applications and documents that could not be published to Kafka stream"

	private val innsendtCounter: Counter = registerCounter(NAME, HELP, LABEL_NAME)
	private val errorCounter: Counter = registerCounter(ERROR, HELP_ERROR, LABEL_NAME)

	private fun registerCounter(name: String, help: String, label: String): Counter =
		Counter
			.build()
			.namespace(SOKNAD_NAMESPACE)
			.subsystem(SUB_SYSTEM)
			.name(name)
			.help(help)
			.labelNames(label)
			.register()

	fun mottattSoknadInc(tema: String) {
		innsendtCounter.labels(tema).inc()
	}

	fun mottattSoknadGet(tema: String) = innsendtCounter.labels(tema).get()

	fun mottattErrorInc(tema: String) {
		errorCounter.labels(tema).inc()
	}

	fun mottattErrorGet(tema: String) = errorCounter.labels(tema).get()
}
