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
	private val labelName = "tema"
	private val appName = "app"
	private val all = "all"

	private val innsendtCounter = mutableMapOf<String, Counter>()

	init {
		try {
			MetricNames.entries.forEach { innsendtCounter[it.name] = registerCounter(it.metricName, it.description) }
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
		mottattSoknadInc(MetricNames.INNSENDT_OK.name, tema)
	}

	fun mottattErrorInc(tema: String) {
		mottattSoknadInc(MetricNames.INNSENDT_ERROR.name, tema)
	}

	fun mottattSoknadInc(name: String, tema: String) {
		val counter = innsendtCounter[name]
		counter?.labelValues(tema, app)?.inc()
		counter?.labelValues(all, app)?.inc()
	}

	fun mottattSoknadGet(tema: String): Double? {
		return mottattSoknadGet(MetricNames.INNSENDT_OK.name, tema)
	}

	fun mottattErrorGet(tema: String): Double? {
		return mottattSoknadGet(MetricNames.INNSENDT_ERROR.name, tema)
	}

	fun mottattSoknadGet(name: String, tema: String): Double? {
		val counter = innsendtCounter[name]
		return counter?.labelValues(tema, app)?.get()
	}

	fun unregister() {
		MetricNames.entries.forEach { innsendtCounter[it.name]?.clear() }
	}
}
