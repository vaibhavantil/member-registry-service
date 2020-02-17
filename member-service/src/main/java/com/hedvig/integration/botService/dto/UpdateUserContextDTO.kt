package com.hedvig.integration.botService.dto

class UpdateUserContextDTO(
    val memberId: String,
    val personalNumber: String?,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val email: String?,
    val street: String?,
    val city: String?,
    val zipCode: String?,
    val hasSigned: Boolean?
)
