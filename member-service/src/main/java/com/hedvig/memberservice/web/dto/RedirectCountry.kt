package com.hedvig.memberservice.web.dto

import com.hedvig.memberservice.commands.models.ZignSecAuthenticationMarket

enum class RedirectCountry {
    NORWAY,
    DENMARK
}

fun RedirectCountry.toZignSecAuthenticationMarket() = when (this) {
    RedirectCountry.NORWAY -> ZignSecAuthenticationMarket.NORWAY
    RedirectCountry.DENMARK -> ZignSecAuthenticationMarket.DENMARK
}
