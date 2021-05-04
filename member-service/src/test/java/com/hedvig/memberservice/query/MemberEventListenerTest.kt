package com.hedvig.memberservice.query

import com.hedvig.integration.botService.BotService
import com.hedvig.integration.productsPricing.CampaignService
import com.hedvig.memberservice.aggregates.FraudulentStatus
import com.hedvig.memberservice.aggregates.MemberStatus
import com.hedvig.memberservice.events.MemberCreatedEvent
import com.hedvig.memberservice.events.MemberDeletedEvent
import com.hedvig.memberservice.events.MemberSignedEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import kotlin.math.sign

@DataJpaTest
class MemberEventListenerTest @Autowired constructor(
    private val memberRepository: MemberRepository,
    private val signedMemberRepository: SignedMemberRepository,
    private val entityManager: TestEntityManager
) {
    private lateinit var eventListener: MemberEventListener

    @MockBean
    private lateinit var trackingIdRepository: TrackingIdRepository

    @MockBean
    private lateinit var campaignService: CampaignService

    @MockBean
    private lateinit var botService: BotService

    @BeforeEach
    fun setup(@Autowired context: ApplicationContext) {
        eventListener = context.autowireCapableBeanFactory.createBean(MemberEventListener::class.java)
    }

    @Test
    fun `should delete member entity on MemberDeletedEvent`() {
        val memberId = 123L
        eventListener.on(MemberCreatedEvent(memberId, MemberStatus.ONBOARDING))
        entityManager.flush()
        entityManager.clear()
        eventListener.on(MemberDeletedEvent(memberId))
        assertThat(memberRepository.findById(memberId)).isEmpty
    }

    @Test
    fun `should delete signed member entity on MemberDeletedEvent`() {
        val memberId = 123L
        eventListener.on(MemberCreatedEvent(memberId, MemberStatus.ONBOARDING))
        eventListener.on(MemberSignedEvent(memberId, "", "", "", null), Instant.now())
        entityManager.flush()
        entityManager.clear()
        eventListener.on(MemberDeletedEvent(memberId))
        assertThat(signedMemberRepository.findById(memberId)).isEmpty
    }

}
