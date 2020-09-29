package com.hedvig.memberservice.services.dto

import com.hedvig.external.authentication.dto.ZignSecAuthenticationResponseError

data class StartNorwegianBankIdSignResponse(
    val redirectUrl: String?,
    val internalErrorMessage: String? = null,
    val errorMessages: List<ZignSecAuthenticationResponseError>? = null
)

//TODO: Guessing we need to add a danish one!
