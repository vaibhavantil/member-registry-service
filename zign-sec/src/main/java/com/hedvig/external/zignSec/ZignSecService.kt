package com.hedvig.external.zignSec

import com.hedvig.external.authentication.NorwegianAuthentication
import com.hedvig.external.zignSec.client.dto.ZignSecNotificationRequest

interface ZignSecService: NorwegianAuthentication {

    fun handleNotification(request: ZignSecNotificationRequest)
}
