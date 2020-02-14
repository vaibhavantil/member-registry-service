package com.hedvig.external.zignSec

import com.hedvig.external.zignSec.client.dto.ZignSecNotificationRequest

interface ZignSecService {

    fun handleNotification(request: ZignSecNotificationRequest)
}
