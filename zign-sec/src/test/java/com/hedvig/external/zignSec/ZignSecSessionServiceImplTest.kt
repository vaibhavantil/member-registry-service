package com.hedvig.external.zignSec

import com.hedvig.external.authentication.dto.NorwegianBankIdAuthenticationRequest
import com.hedvig.external.zignSec.client.ZignSecClient
import com.hedvig.external.zignSec.repository.ZignSecSessionRepository
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner


@RunWith(SpringRunner::class)
class ZignSecServiceImplTest {

    @MockK
    lateinit var sessionRepository: ZignSecSessionRepository

    @MockBean
    lateinit var client: ZignSecClient

    lateinit var classUnderTest: ZignSecServiceImpl

    @Before
    fun before() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        classUnderTest = ZignSecServiceImpl(
            sessionRepository,
            client,
            "authenticate-me",
            "host"
        )
    }

    @Test
    fun auth() {
        val response = classUnderTest.auth(request)

        assert(true)
    }

    @Test
    fun sign() {
    }

    @Test
    fun collect() {
    }

    @Test
    fun handleNotification() {
    }

    companion object {
        val request = NorwegianBankIdAuthenticationRequest(
            "1337",
            "12121212120",
            "NO",
            false
        )
    }
}
