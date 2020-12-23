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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

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
            MockMvcRequestBuilders.post("/_/staging/unsignMember")
                .header("hedvig.token", "1337")
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }
}
