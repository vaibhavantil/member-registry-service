package com.hedvig.memberservice.services.member.dto

data class ErrorResponseDto (
    val errorCode: ErrorCodes,
    val errorMessage: String
)

enum class ErrorCodes {
    MEMBER_HAS_EXISTING_INSURANCE
}
