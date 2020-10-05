package com.hedvig.external.zignSec

import com.hedvig.external.authentication.ZignSecAuthentication

interface ZignSecSessionService: ZignSecAuthentication {

    fun handleNotification(jsonRequest: String)
}
