package no.nav.soknad.arkivering.soknadsmottaker.supervision

import io.prometheus.metrics.core.metrics.Counter
import io.prometheus.metrics.model.registry.PrometheusRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Component
class InnsendtMetrics(private val registry: PrometheusRegistry) {

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
		return Counter.builder()
			.name("${soknadNamespace}_${name}")
			.help(help)
			.labelNames(labelName, appName)
			.withoutExemplars()
			.register(registry)
	}


	fun mottattSoknadInc(tema: String) {
		val counter = innsendtCounter[ok]
		counter?.labelValues(tema, app)?.inc()
		counter?.labelValues(all, app)?.inc()
	}

	fun mottattSoknadGet(tema: String): Double? {
		val counter = innsendtCounter[ok]
		return counter?.labelValues(tema, app)?.get()
	}

	fun mottattErrorInc(tema: String) {
		val counter = innsendtCounter[error]
		counter?.labelValues(tema, app)?.inc()
		counter?.labelValues(all, app)?.inc()
	}

	fun mottattErrorGet(tema: String): Double? {
		val counter = innsendtCounter[error]
		return counter?.labelValues(tema, app)?.get()
	}

	fun unregister() {
		innsendtCounter[ok]?.clear()
		innsendtCounter[error]?.clear()
	}
}
