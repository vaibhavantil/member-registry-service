package com.hedvig.memberservice.events


data class ZignSecSuccessfulAuthenticationEvent(
    val memberId: Long,
    val ssn: String,
    override val providerJsonResponse: String,
    val authenticationMethod: AuthenticationMethod
): ZignSecIdentityJson() {
    enum class AuthenticationMethod {
        NORWEGIAN_BANK_ID,
        DANISH_BANK_ID
    }
}
