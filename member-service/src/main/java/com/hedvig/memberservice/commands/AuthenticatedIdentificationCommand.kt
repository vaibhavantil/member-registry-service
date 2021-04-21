package com.hedvig.memberservice.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier

data class AuthenticatedIdentificationCommand(
    @TargetAggregateIdentifier val id: Long,
    val firstName: String?,
    val lastName: String?,
    val nationalIdentifier: String,
    val countryCode: String,
    val source: Source
) {
    sealed class Source(val identifier: String) {
        object SwedishBankID: Source("com.bankid")
        data class ZignSec(val idProviderName: String): Source("com.zignsec:$idProviderName")
    }
}
