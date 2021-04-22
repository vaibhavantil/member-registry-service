package com.hedvig.memberservice.users

import com.hedvig.auth.services.UserService
import com.hedvig.memberservice.events.MemberSignedEvent
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
internal class MemberSignedEventHandlerTest @Autowired constructor(
    private val userService: UserService,
    private val entityManager: TestEntityManager
) {

    private lateinit var eventHandler: MemberSignedEventHandler

    @BeforeEach
    fun setup(@Autowired context: ApplicationContext) {
        eventHandler = context.autowireCapableBeanFactory.createBean(MemberSignedEventHandler::class.java)
    }

    @Test
    fun `signed member is imported on event`() {
        val memberId = 123L
        val personalNumber = "201212121212"

        eventHandler.on(
            MemberSignedEvent(memberId, "ref", "sig", "oscp", personalNumber)
        )

        val user = userService.findOrCreateUserWithCredential(
            UserService.Credential.SwedishBankID(
                personalNumber = personalNumber
            ),
            UserService.Context(onboardingMemberId = null)
        )
        assertThat(user).isNotNull
        assertThat(user?.associatedMemberId).isEqualTo(memberId.toString())
    }

    @Test
    fun `exporting the same user twice simply ignores second attempt`() {
        val memberId = 123L
        val personalNumber = "201212121212"

        eventHandler.on(
            MemberSignedEvent(memberId, "ref", "sig", "oscp", personalNumber)
        )
        entityManager.clear()
        eventHandler.on(
            MemberSignedEvent(memberId, "ref", "sig", "oscp", personalNumber)
        )
    }
}
