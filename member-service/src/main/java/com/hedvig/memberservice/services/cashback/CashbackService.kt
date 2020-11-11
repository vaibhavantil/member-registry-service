package com.hedvig.memberservice.services.cashback

import com.hedvig.memberservice.web.dto.CashbackOption
import java.util.*

interface CashbackService {
    fun getCashbackOption(memberId: Long): Optional<CashbackOption>
    fun getOptions(memberId: Long): List<CashbackOption>
    fun getDefaultId(memberId: Long): UUID
    fun getDefaultCashback(memberId: Long): CashbackOption?
}
