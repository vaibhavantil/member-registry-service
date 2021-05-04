package com.hedvig.memberservice.users

import com.hedvig.auth.services.UserService
import com.hedvig.memberservice.events.MemberDeletedEvent
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
class MemberDeletedEventHandlerTest @Autowired constructor(
    private val userService: UserService,
    private val entityManager: TestEntityManager
) {

    private lateinit var eventHandler: MemberDeletedEventHandler

    @BeforeEach
    fun setup(@Autowired context: ApplicationContext) {
        eventHandler = context.autowireCapableBeanFactory.createBean(MemberDeletedEventHandler::class.java)
    }

    @Test
    fun `should delete user on MemberDeletedEvent`() {
        val memberId = 123L
        val user = userService.findOrCreateUserWithCredential(
            UserService.Credential.SwedishBankID(personalNumber = "190001010101"),
            UserService.Context(
                onboardingMemberId = "test-member-id"
            )
        )
        entityManager.refresh(user)
        eventHandler.on(MemberDeletedEvent(memberId))
        assertThat(userService.findUserByAssociatedMemberId(memberId = memberId.toString())).isNull()
    }
}
