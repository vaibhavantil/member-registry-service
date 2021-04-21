package com.hedvig.memberservice.query

import com.hedvig.integration.botService.BotService
import com.hedvig.integration.botService.dto.UpdateUserContextDTO
import com.hedvig.integration.productsPricing.dto.EditMemberNameRequestDTO
import com.hedvig.memberservice.aggregates.MemberStatus
import com.hedvig.memberservice.events.MemberCreatedEvent
import com.hedvig.memberservice.events.MemberIdentifiedEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull

@DataJpaTest
@Import(
    MemberEventListener::class,
    MemberEventListenerTest.FakeBotService::class
)
internal class MemberEventListenerTest @Autowired constructor(
    val memberRepository: MemberRepository,
    val listener: MemberEventListener
) {

    @Test
    fun `member created`() {
        listener.on(
            MemberCreatedEvent(123L, MemberStatus.INITIATED)
        )
        val member = memberRepository.findByIdOrNull(123L)
        assertThat(member).isNotNull
    }

    @Test
    fun `member updated from MemberIdentifiedEvent`() {
        listener.on(MemberCreatedEvent(123L, MemberStatus.INITIATED))
        listener.on(
            MemberIdentifiedEvent(
                memberId = 123L,
                nationalIdentification = MemberIdentifiedEvent.NationalIdentification("201212121212", MemberIdentifiedEvent.Nationality.SWEDEN),
                identificationMethod = MemberIdentifiedEvent.IdentificationMethod("com.bankid"),
                firstName = "Test",
                lastName = "Testsson"
            )
        )
        val member = memberRepository.findByIdOrNull(123L)
        assertThat(member).isNotNull
        assertThat(member?.firstName).isEqualTo("Test")
        assertThat(member?.lastName).isEqualTo("Testsson")
        assertThat(member?.ssn).isEqualTo("201212121212")
    }

    class FakeBotService: BotService {
        override fun initBotServiceSessionWebOnBoarding(memberId: Long?, userContext: UpdateUserContextDTO?) {
        }

        override fun editMemberName(memberId: String?, editMemberNameRequestDTO: EditMemberNameRequestDTO?) {
        }

        override fun initBotService(memberId: Long?, json: String?) {
        }
    }
}
