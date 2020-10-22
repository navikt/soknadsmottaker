package no.nav.soknad.arkivering.soknadsmottaker

import io.prometheus.client.Counter

internal object Metrics {


	private val SOKNAD_NAMESPACE = "soknadinnsending"
	private val SUB_SYSTEM = "soknadsMottaker"
	private val NAME = "innsendte"
	private val HELP = "Number of applications and documents received and published to Kafka stream"
	private val LABEL_NAME = "tema"
	private val ERROR = "feil"
	private val HELP_ERROR = "Number of applications and documents failed receive and publish to Kafka stream"

	private val innsendtCounter: Counter = registerCounter(NAME, HELP, LABEL_NAME)
	private val errorCounter: Counter = registerCounter(ERROR, HELP_ERROR, LABEL_NAME)

	internal fun registerCounter(name: String, help: String, label: String): Counter =
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

	fun mottattSoknadGet(tema: String): Double {
		return innsendtCounter.labels(tema).get()
	}

	fun mottattErrorInc(tema: String) {
		errorCounter.labels(tema).inc()
	}

	fun mottattErrorGet(tema: String): Double {
		return errorCounter.labels(tema).get()
	}

}
