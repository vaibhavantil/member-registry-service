package com.hedvig.memberservice.commands.models

import com.hedvig.external.authentication.dto.ZignSecAuthenticationMethod

enum class ZignSecAuthenticationMarket(val countryCode: String) {
    NORWAY("NO"),
    DENMARK("DK");

    fun getAuthenticationMethod() = when (this) {
        NORWAY -> ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
        DENMARK -> ZignSecAuthenticationMethod.DENMARK
    }

    companion object {
        fun fromAuthenticationMethod(method: ZignSecAuthenticationMethod) = when (method) {
            ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE -> NORWAY
            ZignSecAuthenticationMethod.DENMARK -> DENMARK
        }
    }
}
