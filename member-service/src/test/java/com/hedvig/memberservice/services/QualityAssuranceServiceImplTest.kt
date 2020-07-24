package com.hedvig.memberservice.services


import com.hedvig.external.zignSec.repository.ZignSecSignEntityRepository
import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.memberservice.services.exceptions.CountryNotFoundException
import com.hedvig.memberservice.services.exceptions.SignedMemberNotFoundException
import com.hedvig.memberservice.web.QualityAssuranceController
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(MockitoJUnitRunner::class)
class QualityAssuranceServiceImplTest {

    @Mock
    lateinit var signedMemberRepository: SignedMemberRepository

    @Mock
    lateinit var zignSecSignEntityRepository: ZignSecSignEntityRepository

    @Rule
    @JvmField
    var thrown = ExpectedException.none()

    private lateinit var sut: QualityAssuranceServiceImpl

    @Before
    fun setup() {
        sut = QualityAssuranceServiceImpl(signedMemberRepository, zignSecSignEntityRepository)
    }

    @Test
    @Throws(SignedMemberNotFoundException::class)
    fun `When SSN does not exist, throw exception`() {
        thrown.expect(SignedMemberNotFoundException::class.java)
        sut.unsignMember("SWEDEN", "123456")
    }

    @Test
    @Throws(CountryNotFoundException::class)
    fun `When country does not exist, throw exception`() {
        thrown.expect(CountryNotFoundException::class.java)
        sut.unsignMember("ELDORADO", "123456")
    }

}

@RunWith(SpringRunner::class)
@WebMvcTest(controllers = [QualityAssuranceController::class])
@ActiveProfiles("production")
class QualityAssuranceServiceImplProductionTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var qualityAssuranceServiceImpl: QualityAssuranceServiceImpl

    @Test
    fun `When profile is production, the service is NOT available`() {
        mockMvc.perform(
            post("/_/staging/SWEDEN/member/unsign")
                .header("hedvig.token", "1337")
        ).andExpect(status().isNotFound)
    }
}

@WebMvcTest(QualityAssuranceController::class)
@RunWith(SpringRunner::class)
@ActiveProfiles("staging,development")
class QualityAssuranceServiceImplStagingTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var qualityAssuranceServiceImpl: QualityAssuranceServiceImpl

    @Test
    fun `When profile is staging or development, the endpoint IS available`() {
        mockMvc.perform(
            post("/_/staging/SWEDEN/member/unsign")
                .header("hedvig.token", "1337")
        ).andExpect(status().isBadRequest)
    }
}
