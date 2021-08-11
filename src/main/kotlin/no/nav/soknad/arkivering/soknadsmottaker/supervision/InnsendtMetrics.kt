package no.nav.soknad.arkivering.soknadsmottaker.supervision

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

	private val soknadNamespace = "soknadinnsending"
	private val app = "soknadsMottaker"
	private val name = "innsendte"
	private val help = "Number of applications and documents received and published to Kafka stream"
	private val labelName = "tema"
	private val appName = "app"
	private val errorName = "innsendt_feil"
	private val error = "error"
	private val ok = "ok"
	private val all = "all"
	private val helpError = "Number of applications and documents that could not be published to Kafka stream"

	private val innsendtCounter = mutableMapOf<String, Counter>()

	init {
		try {
			innsendtCounter[ok] = registerCounter(name, help)
			innsendtCounter[error] = registerCounter(errorName, helpError)
		} catch (e: Exception) {
			logger.warn("Error while defining counters", e)
		}
	}

	private fun registerCounter(name: String, help: String): Counter {
		return Counter.build()
			.namespace(soknadNamespace)
			.name(name)
			.help(help)
			.labelNames(labelName, appName)
			.register(registry)
	}


	fun mottattSoknadInc(tema: String) {
		val counter = innsendtCounter[ok]
		counter?.labels(tema, app)?.inc()
		counter?.labels(all, app)?.inc()
	}

	fun mottattSoknadGet(tema: String): Double? {
		val counter = innsendtCounter[ok]
		return counter?.labels(tema, app)?.get()
	}

	fun mottattErrorInc(tema: String) {
		val counter = innsendtCounter[error]
		counter?.labels(tema, app)?.inc()
		counter?.labels(all, app)?.inc()
	}

	fun mottattErrorGet(tema: String): Double? {
		val counter = innsendtCounter[error]
		return counter?.labels(tema, app)?.get()
	}

	fun unregister() {
		innsendtCounter[ok]?.clear()
		innsendtCounter[error]?.clear()
	}
}
