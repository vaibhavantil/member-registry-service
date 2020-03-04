package com.hedvig.memberservice.services

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.Mockito.`when` as whenever

@RunWith(MockitoJUnitRunner::class)
class UnderwriterSigningServiceImplTest {

    private lateinit var sut: UnderwriterSigningService

    @Before
    fun setup() {
        sut = UnderwriterSigningServiceImpl()
    }

    @Test
    fun startSwedishBankIdSignSession() {

        sut.startSwedishBankIdSignSession()
    }
}
