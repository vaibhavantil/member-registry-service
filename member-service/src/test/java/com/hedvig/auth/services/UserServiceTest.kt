package com.hedvig.auth.services

import com.hedvig.auth.models.*
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.ApplicationContext

@DataJpaTest
class UserServiceTest {

    lateinit var userService: UserService

    @Autowired
    private lateinit var simpleSignConnectionRepository: SimpleSignConnectionRepository

    @BeforeEach
    fun setup(@Autowired context: ApplicationContext) {
        userService = context.autowireCapableBeanFactory.createBean(UserService::class.java)
    }

    @Test
    fun findOrCreateUserWithSwedishBankIdCredentials_shouldCreateAndReturnNewUser() {
        val user = userService.findOrCreateUserWithCredentials(
            UserService.Credentials.SwedishBankID(personalNumber = "190001010101"),
            onboardingMemberId = "test-member-id"
        )
        Assertions.assertThat(user).isNotNull
        Assertions.assertThat(user?.associatedMemberId).isEqualTo("test-member-id")
    }

    @Test
    fun findOrCreateUserWithSwedishBankIdCredentials_withMatchingPersonalNumber_shouldReturnUser() {
        userService.findOrCreateUserWithCredentials(
            UserService.Credentials.SwedishBankID(personalNumber = "190001010101"),
            onboardingMemberId = "test-member-id"
        )
        val user = userService.findOrCreateUserWithCredentials(
            UserService.Credentials.SwedishBankID(personalNumber = "190001010101"),
            onboardingMemberId = "new-test-member-id"
        )
        Assertions.assertThat(user).isNotNull
        Assertions.assertThat(user?.associatedMemberId).isEqualTo("test-member-id")
    }

    @Test
    fun findOrCreateUserWithSwedishBankIdCredentials_withPersonalNumber_shouldReturnExistingSimpleSignUser() {
        simpleSignConnectionRepository.saveAndFlush(
            SimpleSignConnection(
                user = User(associatedMemberId = "test-simple-sign-member-id"),
                personalNumber = "190001010101",
                country = "SE"
            )
        )
        val user = userService.findOrCreateUserWithCredentials(
            UserService.Credentials.SwedishBankID(personalNumber = "190001010101"),
            onboardingMemberId = "new-test-member-id"
        )
        Assertions.assertThat(user).isNotNull
        Assertions.assertThat(user?.associatedMemberId).isEqualTo("test-simple-sign-member-id")
    }

    @Test
    fun findOrCreateUserWithZignSecCredentials_shouldCreateAndReturnNewUser() {
        val user = userService.findOrCreateUserWithCredentials(
            UserService.Credentials.ZignSec(
                countryCode = "DK",
                idProviderName = "NemID",
                idProviderPersonId = "test-xyz"
            ),
            onboardingMemberId = "test-member-id"
        )
        Assertions.assertThat(user).isNotNull
        Assertions.assertThat(user?.associatedMemberId).isEqualTo("test-member-id")
    }

    @Test
    fun findOrCreateUserWithZignSecCredentials_withMatchingProviderNameAndPersonId_shouldReturnUser() {
        userService.findOrCreateUserWithCredentials(
            UserService.Credentials.ZignSec(
                countryCode = "DK",
                idProviderName = "NemID",
                idProviderPersonId = "test-xyz"
            ),
            onboardingMemberId = "test-member-id"
        )
        val user = userService.findOrCreateUserWithCredentials(
            UserService.Credentials.ZignSec(
                countryCode = "DK",
                idProviderName = "NemID",
                idProviderPersonId = "test-xyz"
            ),
            onboardingMemberId = "new-test-member-id"
        )
        Assertions.assertThat(user).isNotNull
        Assertions.assertThat(user?.associatedMemberId).isEqualTo("test-member-id")
    }

    @Test
    fun findOrCreateUserWithZignSecCredentials_withPersonalNumber_shouldReturnExistingSimpleSignUser() {
        simpleSignConnectionRepository.saveAndFlush(
            SimpleSignConnection(
                user = User(associatedMemberId = "test-simple-sign-member-id"),
                personalNumber = "190001010101",
                country = "DK"
            )
        )
        val user = userService.findOrCreateUserWithCredentials(
            UserService.Credentials.ZignSec(
                countryCode = "DK",
                idProviderName = "NemID",
                idProviderPersonId = "test-xyz",
                personalNumber = "190001010101"
            ),
            onboardingMemberId = "test-member-id"
        )
        Assertions.assertThat(user).isNotNull
        Assertions.assertThat(user?.associatedMemberId).isEqualTo("test-simple-sign-member-id")
    }

}
