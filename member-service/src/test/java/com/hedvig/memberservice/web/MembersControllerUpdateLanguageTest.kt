package com.hedvig.memberservice.web

import com.hedvig.integration.productsPricing.CampaignService
import com.hedvig.memberservice.commands.UpdateAcceptLanguageCommand
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.query.TrackingIdRepository
import com.hedvig.memberservice.services.cashback.CashbackService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.CommandGateway
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@RunWith(SpringRunner::class)
@WebMvcTest(controllers = [MembersController::class])
@ActiveProfiles("test")
class MembersControllerUpdateLanguageTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean(relaxed = true)
    lateinit var commandGateway: CommandGateway

    @MockkBean
    lateinit var memberRepository: MemberRepository

    @MockkBean
    lateinit var campaignService: CampaignService

    @MockkBean
    lateinit var trackingIdRepository: TrackingIdRepository

    @MockkBean
    lateinit var cashbackService: CashbackService

    @Test
    fun postLanguage() {
        val json = """
            {
            "graphqlLocale": "sv_SE"
            }
        """.trimIndent()

        val request = post("/member/language/update")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(json)
            .header("hedvig.token", "1337")

        mockMvc.perform(request)

        val slot = slot<UpdateAcceptLanguageCommand>()
        verify { commandGateway.sendAndWait(capture(slot)) }
        assertThat(slot.captured.acceptLanguage).isEqualTo("sv-SE")
    }

    @Test
    fun `post AcceptLanguage equal to sv_SE`() {
        val json = """
            {
            "acceptLanguage": "sv_SE"
            }
        """.trimIndent()

        val request = post("/member/language/update")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(json)
            .header("hedvig.token", "1337")

        mockMvc.perform(request)

        val slot = slot<UpdateAcceptLanguageCommand>()
        verify { commandGateway.sendAndWait(capture(slot)) }
        assertThat(slot.captured.acceptLanguage).isEqualTo("sv-SE")
    }

    @Test
    fun `post acceptLanguage and graphqlLocale is null returns 400`() {
        val json = """
            {
            }
        """.trimIndent()

        val request = post("/member/language/update")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(json)
            .header("hedvig.token", "1337")

        val result = mockMvc.perform(request).andReturn()

        verify(inverse = true) { commandGateway.sendAndWait(any()) }
        assertThat(result.response.status).isEqualTo(400)
        assertThat(result.response.errorMessage).contains("acceptLanguage", "graphqlLocale")
    }

    @Test
    fun `post with valid but complex acceptLanguage`() {
        val json = """
            {
            "acceptLanguage": "en-GB,en-US;q=0.9,en;q=0.8,sv;q=0.7"
            }
        """.trimIndent()

        val request = post("/member/language/update")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(json)
            .header("hedvig.token", "1337")

        val result = mockMvc.perform(request).andReturn()

        val slot = slot<UpdateAcceptLanguageCommand>()
        verify { commandGateway.sendAndWait(capture(slot)) }
        assertThat(slot.captured.acceptLanguage).isEqualTo("en-GB,en-US;q=0.9,en;q=0.8,sv;q=0.7")
    }

    @Test
    fun `post with both graphqlLocale and acceptLanguage prefer graphqlLocale`() {
        val json = """
            {
            "graphqlLocale": "sv_SE",
            "acceptLanguage": "en-GB,en-US;q=0.9,en;q=0.8,sv;q=0.7"
            }
        """.trimIndent()

        val request = post("/member/language/update")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(json)
            .header("hedvig.token", "1337")

        val result = mockMvc.perform(request).andReturn()

        val slot = slot<UpdateAcceptLanguageCommand>()
        verify { commandGateway.sendAndWait(capture(slot)) }
        assertThat(slot.captured.acceptLanguage).isEqualTo("sv-SE")
    }
}
