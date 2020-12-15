package com.hedvig.memberservice.services.signing.underwriter.strategy

sealed class UnderwriterSessionCompletedData {

    data class SwedishBankId(
        val referenceToken: String,
        val signature: String,
        val oscpResponse: String
    ): UnderwriterSessionCompletedData()

    object BankIdRedirect : UnderwriterSessionCompletedData()

    object SimpleSign : UnderwriterSessionCompletedData()

}
