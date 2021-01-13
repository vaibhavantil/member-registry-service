package com.hedvig.memberservice.commands.models

import com.hedvig.external.authentication.dto.ZignSecAuthenticationMethod
import com.hedvig.memberservice.events.MemberIdentifiedEvent

enum class ZignSecAuthenticationMarket {
    NORWAY,
    DENMARK;

    fun getAuthenticationMethod() = when (this) {
        NORWAY -> ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
        DENMARK -> ZignSecAuthenticationMethod.DENMARK
    }

    fun toMemberIdentifiedEventIdentificationMethod() = when (this) {
        ZignSecAuthenticationMarket.NORWAY -> MemberIdentifiedEvent.IdentificationMethod.NORWEGIAN_BANK_ID
        ZignSecAuthenticationMarket.DENMARK -> MemberIdentifiedEvent.IdentificationMethod.DANISH_BANK_ID
    }

    companion object {
        fun fromAuthenticationMethod(method: ZignSecAuthenticationMethod) = when (method) {
            ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE -> NORWAY
            ZignSecAuthenticationMethod.DENMARK -> DENMARK
        }
    }
}
