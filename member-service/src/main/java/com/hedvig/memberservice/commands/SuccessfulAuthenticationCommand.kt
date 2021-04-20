package com.hedvig.memberservice.commands

import com.hedvig.external.bankID.bankIdTypes.Collect.User
import com.hedvig.external.zignSec.repository.entitys.Identity
import com.hedvig.memberservice.commands.models.ZignSecAuthenticationMarket
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class AuthenticatedIdentificationCommand(
    @TargetAggregateIdentifier val id: Long,
    val firstName: String?,
    val lastName: String?,
    val personalNumber: String,
    val countryCode: String,
    val source: Source
) {
    sealed class Source {
        object SwedishBankID: Source()
        data class ZignSec(val idProviderName: String): Source()
    }
}

data class SuccessfulAuthenticationCommand(
    @TargetAggregateIdentifier val id: Long,
    val authentication: Authentication
) {

    sealed class Authentication {
        data class SwedishBankID(
            val user: User
        ): Authentication()

        data class ZignSec(
            val personalNumber: String,
            val identity: Identity,
            val market: ZignSecAuthenticationMarket
        ): Authentication()
    }
}
