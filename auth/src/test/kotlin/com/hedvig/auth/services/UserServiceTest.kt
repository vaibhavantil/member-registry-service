package com.hedvig.auth.services

import com.hedvig.auth.models.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.ApplicationContext

@DataJpaTest(
    properties = ["hedvig.auth.canCreateUsersOnLogin=true"]
)
class UserServiceTest {

    lateinit var userService: UserService

    @Autowired
    private lateinit var simpleSignConnectionRepository: SimpleSignConnectionRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @BeforeEach
    fun setup(@Autowired context: ApplicationContext) {
        userService = context.autowireCapableBeanFactory.createBean(UserService::class.java)
    }

    @Test
    fun findOrCreateUserWithSwedishBankIdCredentials_shouldCreateAndReturnNewUser() {
        val user = userService.findOrCreateUserWithCredential(
            UserService.Credential.SwedishBankID(personalNumber = "190001010101"),
            UserService.Context(
                onboardingMemberId = "test-member-id"
            )
        )
        assertThat(user).isNotNull
        assertThat(user?.associatedMemberId).isEqualTo("test-member-id")
    }

    @Test
    fun findOrCreateUserWithSwedishBankIdCredentials_withMatchingPersonalNumber_shouldReturnUser() {
        userService.findOrCreateUserWithCredential(
            UserService.Credential.SwedishBankID(personalNumber = "190001010101"),
            UserService.Context(
                onboardingMemberId = "test-member-id"
            )
        )
        val user = userService.findOrCreateUserWithCredential(
            UserService.Credential.SwedishBankID(personalNumber = "190001010101"),
            UserService.Context(
                onboardingMemberId = "new-test-member-id"
            )
        )
        assertThat(user).isNotNull
        assertThat(user?.associatedMemberId).isEqualTo("test-member-id")
    }

    @Test
    fun findOrCreateUserWithSwedishBankIdCredentials_withSimpleSignFallback_shouldReturnSimpleSignUser() {
        simpleSignConnectionRepository.saveAndFlush(
            SimpleSignConnection(
                user = User(associatedMemberId = "test-simple-sign-member-id"),
                personalNumber = "190001010101",
                country = "SE"
            )
        )
        val user = userService.findOrCreateUserWithCredential(
            UserService.Credential.SwedishBankID(
                personalNumber = "190001010101",
                simpleSignFallback = UserService.Credential.SimpleSign(
                    countryCode = "SE",
                    personalNumber = "190001010101"
                )
            ),
            UserService.Context(
                onboardingMemberId = "new-test-member-id"
            )
        )
        assertThat(user).isNotNull
        assertThat(user?.associatedMemberId).isEqualTo("test-simple-sign-member-id")
    }

    @Test
    fun findOrCreateUserWithZignSecCredentials_shouldCreateAndReturnNewUser() {
        val user = userService.findOrCreateUserWithCredential(
            UserService.Credential.ZignSec(
                idProviderName = "NemID",
                idProviderPersonId = "test-xyz"
            ),
            UserService.Context(
                onboardingMemberId = "test-member-id"
            )
        )
        assertThat(user).isNotNull
        assertThat(user?.associatedMemberId).isEqualTo("test-member-id")
    }

    @Test
    fun findOrCreateUserWithZignSecCredentials_withMatchingProviderNameAndPersonId_shouldReturnUser() {
        userService.findOrCreateUserWithCredential(
            UserService.Credential.ZignSec(
                idProviderName = "NemID",
                idProviderPersonId = "test-xyz"
            ),
            UserService.Context(
                onboardingMemberId = "test-member-id"
            )
        )
        val user = userService.findOrCreateUserWithCredential(
            UserService.Credential.ZignSec(
                idProviderName = "NemID",
                idProviderPersonId = "test-xyz"
            ),
            UserService.Context(
                onboardingMemberId = "new-test-member-id"
            )
        )
        assertThat(user).isNotNull
        assertThat(user?.associatedMemberId).isEqualTo("test-member-id")
    }

    @Test
    fun findOrCreateUserWithZignSecCredentials_withSimpleSignFallback_shouldReturnSimpleSignUser() {
        simpleSignConnectionRepository.saveAndFlush(
            SimpleSignConnection(
                user = User(associatedMemberId = "test-simple-sign-member-id"),
                personalNumber = "190001010101",
                country = "DK"
            )
        )
        val user = userService.findOrCreateUserWithCredential(
            UserService.Credential.ZignSec(
                idProviderName = "NemID",
                idProviderPersonId = "test-xyz",
                simpleSignFallback = UserService.Credential.SimpleSign(
                    countryCode = "DK",
                    personalNumber = "190001010101"
                )
            ),
            UserService.Context(
                onboardingMemberId = "test-member-id"
            )
        )
        assertThat(user).isNotNull
        assertThat(user?.associatedMemberId).isEqualTo("test-simple-sign-member-id")
    }

    @Test
    fun findUserWithAssociatedMemberId_shouldReturnUser() {
        userService.findOrCreateUserWithCredential(
            UserService.Credential.SwedishBankID(personalNumber = "190001010101"),
            UserService.Context(
                onboardingMemberId = "test-member-id"
            )
        )
        val user = userService.findUserByAssociatedMemberId("test-member-id")
        assertThat(user).isNotNull
    }

    @Test
    fun deleteUserWithAssociatedMemberId_shouldDeleteUser() {
        val user = userService.findOrCreateUserWithCredential(
            UserService.Credential.SwedishBankID(personalNumber = "190001010101"),
            UserService.Context(
                onboardingMemberId = "test-member-id"
            )
        )
        entityManager.refresh(user)
        userService.deleteUserWithAssociatedMemberId("test-member-id")
        assertThat(userService.findUserByAssociatedMemberId("test-member-id")).isNull()
    }

}
