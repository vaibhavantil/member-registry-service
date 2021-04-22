package com.hedvig.memberservice.commands

import com.hedvig.external.bankID.bankIdTypes.CompletionData
import com.hedvig.external.zignSec.client.dto.ZignSecIdentity
import com.neovisionaries.i18n.CountryCode
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class SuccessfulAuthenticationCommand(
    @TargetAggregateIdentifier val id: Long,
    val method: AuthMethod
) {

    val identity: Identity get() = when (method) {
        is AuthMethod.SwedishBankID -> Identity(
            firstName = method.completionData.user.givenName,
            lastName = method.completionData.user.surname,
            nationalIdentifier = method.completionData.user.personalNumber,
            countryCode = CountryCode.SE
        )
        is AuthMethod.ZignSec -> Identity(
            firstName = method.identity.firstName,
            lastName = method.identity.lastName,
            nationalIdentifier = method.personalNumber,
            countryCode = CountryCode.getByAlpha2Code(method.identity.countryCode)
        )
    }

    sealed class AuthMethod(
        val identifier: String
    ) {
        data class SwedishBankID(
            val completionData: CompletionData
        ): AuthMethod(
            "com.bankid"
        )

        data class ZignSec(
            val personalNumber: String,
            val identity: ZignSecIdentity
        ): AuthMethod(
            "com.zignsec:${identity.idProviderName ?: "unknown"}"
        )
    }

    data class Identity(
        val firstName: String?,
        val lastName: String?,
        val nationalIdentifier: String,
        val countryCode: CountryCode
    )
}
