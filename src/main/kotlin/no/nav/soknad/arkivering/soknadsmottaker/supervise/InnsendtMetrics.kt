package no.nav.soknad.arkivering.soknadsmottaker.supervise

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Component
class InnsendtMetrics(private val registry: CollectorRegistry) {

	private val logger = LoggerFactory.getLogger(javaClass)

	val SOKNAD_NAMESPACE = "soknadinnsending"
	private val APP = "soknadsMottaker"
	val NAME = "innsendte"
	private val HELP = "Number of applications and documents received and published to Kafka stream"
	private val LABEL_NAME = "tema"
	private val ERROR_NAME = "innsendt_feil"
	private val ERROR = "error"
	private val OK = "ok"
	private val ALL = "all"
	private val HELP_ERROR = "Number of applications and documents that could not be published to Kafka stream"

	val innsendtCounter = mutableMapOf<String, Counter>()

	init {
		try {
			innsendtCounter.put(OK, registerCounter(NAME, HELP, LABEL_NAME))
			innsendtCounter.put(ERROR, registerCounter(ERROR_NAME, HELP_ERROR, LABEL_NAME))
		} catch (ex: Exception) {
			logger.warn("Error while defining counters")
		}
	}

	private fun registerCounter(name: String, help: String, label: String): Counter {
		logger.info("Registrerer counter $name med label $label")
		return Counter.build()
			.namespace(SOKNAD_NAMESPACE)
			.name(name)
			.help(help)
			.labelNames(label, "app")
			.register(registry)
	}


	fun mottattSoknadInc(tema: String) {
		val counter = innsendtCounter.get(OK)
		counter?.labels(tema, APP)?.inc()
		counter?.labels(ALL, APP)?.inc()

	}

	fun mottattSoknadGet(tema: String): Double? {
		val counter = innsendtCounter.get(OK)
		return counter?.labels(tema, APP)?.get()
	}

	fun mottattErrorInc(tema: String) {
		val counter = innsendtCounter.get(ERROR)
		counter?.labels(tema, APP)?.inc()
		counter?.labels(ALL, APP)?.inc()
	}

	fun mottattErrorGet(tema: String): Double? {
		val counter = innsendtCounter.get(ERROR)
		return counter?.labels(tema, APP)?.get()
	}

	fun unregister() {
		innsendtCounter.get(OK)?.clear()
		innsendtCounter.get(ERROR)?.clear()
	}

}
