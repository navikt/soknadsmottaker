package no.nav.soknad.arkivering.soknadsmottaker.supervision

enum class MetricNames(val metricName: String, val description: String) {
	INNSENDT_OK("innsendte", "Number of applications and documents received and published to Kafka topic"),
	INNSENDT_UINNLOGGET("innsendte_uinnlogget", "Number of applications and documents received and published to Kafka topic"),
	INNSENDT_ERROR("innsendt_feil", "Number of applications and documents that could not be published to Kafka topic"),
	INNSENDT_UINNLOGGET_ERROR("innsendt_feil_uinnlogget", "Number of applications and documents that could not be published to Kafka topic")
}

