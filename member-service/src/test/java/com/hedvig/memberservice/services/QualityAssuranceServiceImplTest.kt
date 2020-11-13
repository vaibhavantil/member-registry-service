package com.hedvig.memberservice.services

import com.hedvig.memberservice.web.QualityAssuranceController
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

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
            post("/_/staging/SWEDEN/unsignMember")
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
            post("/_/staging/unsignMember")
                .header("hedvig.token", "1337")
        ).andExpect(status().isBadRequest)
    }
}
