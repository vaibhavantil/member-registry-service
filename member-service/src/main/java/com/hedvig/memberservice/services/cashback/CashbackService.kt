package com.hedvig.memberservice.services.cashback

import com.hedvig.memberservice.web.dto.CashbackOption
import java.util.*

interface CashbackService {
    fun selectCashbackOption(memberId: Long, uuid: UUID): Boolean
    fun getMembersCashbackOption(memberId: Long): Optional<CashbackOption>
    fun getOptions(memberId: Long): List<CashbackOption>
    fun getDefaultId(memberId: Long): UUID
    fun getDefaultCashback(memberId: Long): CashbackOption?
}
