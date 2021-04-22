package com.hedvig.memberservice.commands.models

import com.hedvig.external.authentication.dto.ZignSecAuthenticationMethod
import com.neovisionaries.i18n.CountryCode

enum class ZignSecAuthenticationMarket(val countryCode: CountryCode) {
    NORWAY(CountryCode.NO),
    DENMARK(CountryCode.DK);

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
