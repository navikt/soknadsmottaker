package no.nav.soknad.arkivering.soknadsmottaker.schedule

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class LeaderElection(
	val name: String,
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	val lastUpdate: LocalDateTime? = LocalDateTime.now()
)
