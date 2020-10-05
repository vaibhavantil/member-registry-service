package com.hedvig.external.authentication.dto

enum class ZignSecAuthenticationMethod(val zignSecMethodName: String) {
    NORWAY_WEB_OR_MOBILE("nbid_oidc"),
    DENMARK("nemid")
}
