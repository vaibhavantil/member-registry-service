package com.hedvig.memberservice.services.dto

import com.hedvig.external.authentication.dto.NorwegianAuthenticationResponseError

data class StartNorwegianBankIdSignResponse(
    val redirectUrl: String?,
    val errorMessages: List<NorwegianAuthenticationResponseError>? = null
)
