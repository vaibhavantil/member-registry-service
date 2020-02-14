package com.hedvig.external.zignSec

import com.hedvig.external.zignSec.client.dto.ZignSecNotificationRequest
//import com.hedvig.external.zignSec.repository.ZignSecRepository
import org.springframework.stereotype.Service

@Service
class ZignSecServicImpl(
//    private val zignSecRepository: ZignSecRepository
): ZignSecService {
    override fun handleNotification(request: ZignSecNotificationRequest) {
        TODO("Store notification entity to zignSecRepository")
    }
}
