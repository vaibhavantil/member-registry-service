package com.hedvig.memberservice.services

import com.hedvig.localization.service.LocalizationService
import com.hedvig.memberservice.aggregates.PickedLocale
import org.junit.Assert
import org.junit.Test
import java.util.*

class CashbackServiceTest {
    @Test
    fun cashbackOption_RETURNS_Empty_WHEN_CashbackIdNotFound() {
            val service = CashbackService(MockLocalizationService())
            val acctual = service.getCashbackOption(UUID.randomUUID(), PickedLocale.sv_SE)
            Assert.assertFalse(acctual.isPresent)
        }
}

class MockLocalizationService: LocalizationService {
    override fun getText(locale: Locale?, key: String) = "mock"
}
