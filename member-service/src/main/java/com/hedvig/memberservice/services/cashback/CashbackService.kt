package com.hedvig.memberservice.services.cashback

import com.hedvig.memberservice.aggregates.PickedLocale
import com.hedvig.memberservice.web.dto.CashbackOption
import java.util.*

interface CashbackService {
    fun selectCashbackOption(memberId: Long, uuid: UUID, localeOverride: PickedLocale?): Boolean
    fun getOptions(memberId: Long, localeOverride: PickedLocale?): List<CashbackOption>

    fun getMembersCashbackOption(memberId: Long): Optional<CashbackOption>
    fun getDefaultId(memberId: Long): UUID
    fun getDefaultCashback(memberId: Long): CashbackOption?
}
