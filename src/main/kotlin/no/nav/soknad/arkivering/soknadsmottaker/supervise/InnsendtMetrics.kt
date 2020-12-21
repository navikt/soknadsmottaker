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

	final val SOKNAD_NAMESPACE = "soknadinnsending"
	final private val APP = "soknadsMottaker"
	final val NAME = "innsendte"
	final private val HELP = "Number of applications and documents received and published to Kafka stream"
	final private val LABEL_NAME = "tema"
	final private val APP_NAME = "app"
	final private val ERROR_NAME = "innsendt_feil"
	final private val ERROR = "error"
	final private val OK = "ok"
	final private val ALL = "all"
	final private val HELP_ERROR = "Number of applications and documents that could not be published to Kafka stream"

	final private val innsendtCounter = mutableMapOf<String, Counter>()

	init {
		try {
			innsendtCounter[OK] = registerCounter(NAME, HELP)
			innsendtCounter[ERROR] = registerCounter(ERROR_NAME, HELP_ERROR)
		} catch (ex: Exception) {
			logger.warn("Error while defining counters")
		}
	}

	private fun registerCounter(name: String, help: String): Counter {
		return Counter.build()
			.namespace(SOKNAD_NAMESPACE)
			.name(name)
			.help(help)
			.labelNames(LABEL_NAME, APP_NAME)
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
		innsendtCounter[OK]?.clear()
		innsendtCounter[ERROR]?.clear()
	}

}
