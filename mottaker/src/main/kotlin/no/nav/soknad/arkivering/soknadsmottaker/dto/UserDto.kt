package no.nav.soknad.arkivering.soknadsmottaker.dto

data class UserDto(
	val innsendingRef: String, val userId: String, val schema: String, val language: String?
)
