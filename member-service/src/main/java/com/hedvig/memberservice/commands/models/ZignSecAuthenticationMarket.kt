package com.hedvig.memberservice.commands.models

import com.hedvig.external.authentication.dto.ZignSecAuthenticationMethod
import com.hedvig.memberservice.events.ZignSecSuccessfulAuthenticationEvent

enum class ZignSecAuthenticationMarket {
    NORWAY,
    DENMARK;

    fun getAuthenticationMethod() = when (this) {
        NORWAY -> ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
        DENMARK -> ZignSecAuthenticationMethod.DENMARK
    }

    fun toAuthenticationEventAuthenticationMethod() = when (this) {
        ZignSecAuthenticationMarket.NORWAY -> ZignSecSuccessfulAuthenticationEvent.AuthenticationMethod.NORWEGIAN_BANK_ID
        ZignSecAuthenticationMarket.DENMARK -> ZignSecSuccessfulAuthenticationEvent.AuthenticationMethod.DANISH_BANK_ID
    }

    companion object {
        fun fromAuthenticationMethod(method: ZignSecAuthenticationMethod) = when (method) {
            ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE -> NORWAY
            ZignSecAuthenticationMethod.DENMARK -> DENMARK
        }
    }
}
