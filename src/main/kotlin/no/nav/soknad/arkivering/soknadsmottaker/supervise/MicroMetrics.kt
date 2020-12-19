package no.nav.soknad.arkivering.soknadsmottaker.supervise

import io.micrometer.core.instrument.Counter
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.slf4j.LoggerFactory

internal object MicroMetrics {

	private val logger = LoggerFactory.getLogger(javaClass)

	private const val SOKNAD_NAMESPACE = "soknadinnsendingM"
	private const val SUB_SYSTEM = "soknadsMottaker"
	private const val NAME = "innsendte"
	private const val HELP = "Number of applications and documents received and published to Kafka stream"
	private const val LABEL_NAME = "tema"
	private const val ERROR = "feil"
	private const val HELP_ERROR = "Number of applications and documents that could not be published to Kafka stream"
	private val reg = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

	private val innsendtCounter: Counter = registerCounter(NAME, HELP, LABEL_NAME)
	private val errorCounter: Counter = registerCounter(ERROR, HELP_ERROR, LABEL_NAME)

	private fun registerCounter(name: String, help: String, label: String): Counter {
		logger.info("Registrerer counter $name med label $label")
		return Counter.builder(SOKNAD_NAMESPACE+"_"+name).description(help).tag(label,"all").register(reg)
	}

	fun mottattSoknadInc(tema: String) {
		innsendtCounter.increment()
	}

	fun mottattSoknadGet(tema: String) = innsendtCounter.count()

	fun mottattErrorInc(tema: String) {
		errorCounter.increment()
	}

	fun mottattErrorGet(tema: String) = errorCounter.count()

}
