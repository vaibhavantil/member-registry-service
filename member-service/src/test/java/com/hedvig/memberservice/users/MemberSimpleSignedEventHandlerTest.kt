package com.hedvig.memberservice.users

import com.hedvig.auth.services.UserService
import com.hedvig.memberservice.events.MemberSignedWithoutBankId
import com.hedvig.memberservice.events.MemberSimpleSignedEvent
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(UserService::class)
internal class MemberSimpleSignedEventHandlerTest @Autowired constructor(
    private val userService: UserService,
    private val entityManager: TestEntityManager
) {

    private lateinit var eventHandler: MemberSimpleSignedEventHandler

    @BeforeEach
    fun setup(@Autowired context: ApplicationContext) {
        eventHandler = context.autowireCapableBeanFactory.createBean(MemberSimpleSignedEventHandler::class.java)
    }

    @Test
    fun `MemberSimpleSignedEvent - signed member is imported on event`() {
        val memberId = 123L
        val personalNumber = "01129955131"

        eventHandler.on(
            MemberSimpleSignedEvent(
                memberId,
                personalNumber,
                MemberSimpleSignedEvent.Nationality.NORWAY,
                UUID.randomUUID()
            )
        )

        val user = userService.findOrCreateUserWithCredential(
            UserService.Credential.SimpleSign(
                countryCode = "NO",
                personalNumber = personalNumber
            ),
            UserService.Context(onboardingMemberId = null)
        )

        assertThat(user).isNotNull
        assertThat(user?.associatedMemberId).isEqualTo(memberId.toString())
    }

    @Test
    fun `MemberSimpleSignedEvent - signed member again simply ignores second attempt`() {
        val memberId = 123L
        val personalNumber = "01129955131"

        eventHandler.on(
            MemberSimpleSignedEvent(
                memberId,
                personalNumber,
                MemberSimpleSignedEvent.Nationality.NORWAY,
                UUID.randomUUID()
            )
        )
        entityManager.clear()
        eventHandler.on(
            MemberSimpleSignedEvent(
                memberId,
                personalNumber,
                MemberSimpleSignedEvent.Nationality.NORWAY,
                UUID.randomUUID()
            )
        )
    }

    @Test
    fun `MemberSignedWithoutBankId - signed member is imported on event`() {
        val memberId = 123L
        val personalNumber = "01129955131"

        eventHandler.on(
            MemberSignedWithoutBankId(memberId, personalNumber)
        )

        val user = userService.findOrCreateUserWithCredential(
            UserService.Credential.SimpleSign(
                countryCode = "SE",
                personalNumber = personalNumber
            ),
            UserService.Context(onboardingMemberId = null)
        )
        assertThat(user).isNotNull
        assertThat(user?.associatedMemberId).isEqualTo(memberId.toString())
    }

    @Test
    fun `MemberSignedWithoutBankId - signed member is simply ignored second attempt`() {
        val memberId = 123L
        val personalNumber = "01129955131"

        eventHandler.on(
            MemberSignedWithoutBankId(memberId, personalNumber)
        )
        entityManager.clear()
        eventHandler.on(
            MemberSignedWithoutBankId(memberId, personalNumber)
        )
    }
}
