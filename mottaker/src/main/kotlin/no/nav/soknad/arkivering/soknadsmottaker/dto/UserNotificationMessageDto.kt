package no.nav.soknad.arkivering.soknadsmottaker.dto

data class UserNotificationMessageDto(
	val userMessage: UserMessageDto, val userMessage_en: UserMessageDto,
	val messageLinkBase: String, val userList: List<UserDto>
)
