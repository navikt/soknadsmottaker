package no.nav.soknad.arkivering.soknadsmottaker

import io.prometheus.client.Counter

internal object Metrics {

	private var temaCounterMap: MutableMap<String, Counter> = HashMap()

	private val SOKNAD_NAMESPACE = "soknadinnsending"
	private val SUB_SYSTEM = "soknadsMottaker"

	internal fun registerCounter(tema: String): Counter =
	 Counter
		.build()
		.namespace(SOKNAD_NAMESPACE)
		.subsystem(SUB_SYSTEM)
		.name(tema)
		.help("Number of received applications and documents of theme ${tema} received and published to Kafka stream")
		.register()

	fun mottattSoknadInc(tema: String) {
		incrementCounter(tema)
		incrementCounter("ALL")
	}

	private fun incrementCounter(tema: String) {
		if (!temaCounterMap.containsKey(tema)) {
			temaCounterMap.put(tema, registerCounter(tema))
		}
		temaCounterMap.get(tema)?.inc()
	}

}
