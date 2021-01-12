package com.hedvig.memberservice.events


data class ZignSecSuccessfulAuthenticationEvent(
    val memberId: Long,
    val ssn: String,
    val providerJsonResponse: String,
    val authenticationMethod: AuthenticationMethod
) {
    enum class AuthenticationMethod {
        NORWEGIAN_BANK_ID,
        DANISH_BANK_ID
    }
}
