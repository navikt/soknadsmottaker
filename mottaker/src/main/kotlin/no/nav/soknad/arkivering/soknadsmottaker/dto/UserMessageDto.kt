package no.nav.soknad.arkivering.soknadsmottaker.dto

data class UserMessageDto(val message: String, val smsTitle: String?, val smsText: String?, val emailTitle: String?, val emailText: String?)
