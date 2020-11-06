package com.hedvig.memberservice.services

import com.hedvig.common.localization.LocalizationService
import com.hedvig.memberservice.aggregates.PickedLocale
import com.hedvig.memberservice.services.cashback.LocalizedCashbackService
import org.junit.Assert
import org.junit.Test
import java.util.*

class LocalizedCashbackServiceTest {
    @Test
    fun cashbackOption_RETURNS_Empty_WHEN_CashbackIdNotFound() {
            val service = LocalizedCashbackService(MockLocalizationService())
            val acctual = service.getCashbackOption(UUID.randomUUID(), PickedLocale.sv_SE)
            Assert.assertFalse(acctual.isPresent)
        }
}

class MockLocalizationService: LocalizationService {
    override fun getTranslation(key: String, locale: Locale) = "mock"
}
