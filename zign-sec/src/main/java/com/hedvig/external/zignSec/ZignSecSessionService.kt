package com.hedvig.external.zignSec

import com.hedvig.external.authentication.NorwegianAuthentication
import com.hedvig.external.zignSec.client.dto.ZignSecNotificationRequest

interface ZignSecSessionService: NorwegianAuthentication {

    fun handleNotification(request: ZignSecNotificationRequest)
}
