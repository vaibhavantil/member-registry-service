package com.hedvig.external.zignSec

import com.hedvig.external.authentication.dto.NorwegianBankIdAuthenticationRequest
import com.hedvig.external.zignSec.repository.ZignSecSessionRepository
import com.ninjasquad.springmockk.MockkBean
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
class ZignSecSessionServiceImplTest {

    @MockkBean
    lateinit var sessionRepository: ZignSecSessionRepository

//    @MockkBean
//    lateinit var zignSecService: ZignSecService

//    @Autowired
//    lateinit var zignSecSessionService: ZignSecSessionServiceImpl


    @Test
    fun auth() {
//        var classUnderTest = ZignSecSessionServiceImpl(sessionRepository, zignSecService)
//        val response = classUnderTest.auth(request)

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
