package com.hedvig.memberservice.services


import com.hedvig.external.zignSec.repository.ZignSecSignEntityRepository
import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.memberservice.services.member.CannotSignInsuranceException
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.test.context.ActiveProfiles


@RunWith(MockitoJUnitRunner::class)
@ActiveProfiles("development")
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
    @Throws(RuntimeException::class)
    fun `When SSN does not exist, throw exception`() {
        sut.unsignMember("SWEDEN", "123456")
        return
    }

    @Test
    fun `When profile is production, the service is not available`() {
        TODO("Implement something")
    }


    companion object {

    }
}
