package com.hedvig.auth.models

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant

@DataJpaTest
internal class SwedishBankIdCredentialTest {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var swedishBankIdCredentialRepository: SwedishBankIdCredentialRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    fun `can create SwedishBankIdCredential for a User with personalNumber`() {
        val user = userRepository.saveAndFlush(User(associatedMemberId = "test-member-id"))
        val credential = swedishBankIdCredentialRepository.saveAndFlush(
            SwedishBankIdCredential(
                personalNumber = "2002022002",
                user = user
            )
        )
        Assertions.assertThat(credential.id).isNotEqualTo(0)
    }

    @Test
    fun `created SwedishBankIdCredential can be retrieved by personalNumber`() {
        swedishBankIdCredentialRepository.saveAndFlush(
            SwedishBankIdCredential(
                personalNumber = "2002022002",
                user = User(associatedMemberId = "test-member-id")
            )
        )
        val credential = swedishBankIdCredentialRepository.findByPersonalNumber("2002022002")
        Assertions.assertThat(credential).isNotNull()
    }

    @Test
    fun `retrieves null if no SwedishBankIdCredential exists by personalNumber`() {
        val credential = swedishBankIdCredentialRepository.findByPersonalNumber("2002022002")
        Assertions.assertThat(credential).isNull()
    }

    @Test
    fun `can not create BankIdCredentials with duplicate personalNumber`() {
        swedishBankIdCredentialRepository.saveAndFlush(
            SwedishBankIdCredential(
                personalNumber = "2002022002",
                user = User(associatedMemberId = "test-member-id")
            )
        )
        assertThrows<DataIntegrityViolationException> {
            swedishBankIdCredentialRepository.saveAndFlush(
                SwedishBankIdCredential(
                    personalNumber = "2002022002",
                    user = User(associatedMemberId = "test-member-id-2")
                )
            )
        }
    }

    @Test
    fun `can not create two BankIdCredentials same user`() {
        val user = User(associatedMemberId = "test-member-id")
        swedishBankIdCredentialRepository.saveAndFlush(
            SwedishBankIdCredential(
                personalNumber = "2002022002",
                user = user
            )
        )
        assertThrows<DataIntegrityViolationException> {
            swedishBankIdCredentialRepository.saveAndFlush(
                SwedishBankIdCredential(
                    personalNumber = "1001011001",
                    user = user
                )
            )
        }
    }

    @Test
    fun `can not create BankIdCredentials with duplicate associatedMemberId`() {
        swedishBankIdCredentialRepository.saveAndFlush(
            SwedishBankIdCredential(
                personalNumber = "2002022002",
                user = User(associatedMemberId = "test-member-id")
            )
        )
        assertThrows<DataIntegrityViolationException> {
            swedishBankIdCredentialRepository.saveAndFlush(
                SwedishBankIdCredential(
                    personalNumber = "1001011001",
                    user = User(associatedMemberId = "test-member-id")
                )
            )
        }
    }

    @Test
    fun `created SwedishBankIdCredential receive a createdAt timestamp`() {
        val before = Instant.now()
        val createdCredential = swedishBankIdCredentialRepository.saveAndFlush(
            SwedishBankIdCredential(
                personalNumber = "2002022002",
                user = User(associatedMemberId = "test-member-id")
            )
        )
        val after = Instant.now()
        Assertions.assertThat(createdCredential.createdAt).isAfterOrEqualTo(before)
        Assertions.assertThat(createdCredential.createdAt).isBeforeOrEqualTo(after)
    }

    @Test
    fun `can retrieve associatedMemberId through SwedishBankIdCredential using personal number`() {
        swedishBankIdCredentialRepository.saveAndFlush(
            SwedishBankIdCredential(
                personalNumber = "2002022002",
                user = User(associatedMemberId = "test-member-id")
            )
        )
        val credential = swedishBankIdCredentialRepository.findByPersonalNumber("2002022002")
        Assertions.assertThat(credential).isNotNull()
        Assertions.assertThat(credential!!.user.associatedMemberId).isEqualTo("test-member-id")
    }

    @Test
    fun `can delete SwedishBankIdCredential`() {
        val credential = swedishBankIdCredentialRepository.saveAndFlush(
            SwedishBankIdCredential(
                personalNumber = "2002022002",
                user = User(associatedMemberId = "test-member-id")
            )
        )
        swedishBankIdCredentialRepository.delete(credential)
        val credentialAfterDeletion = swedishBankIdCredentialRepository.findByIdOrNull(credential.id)
        Assertions.assertThat(credentialAfterDeletion).isNull()
    }

    @Test
    fun `does not delete User when SwedishBankIdCredential is deleted`() {
        val credential = swedishBankIdCredentialRepository.saveAndFlush(
            SwedishBankIdCredential(
                personalNumber = "2002022002",
                user = User(associatedMemberId = "test-member-id")
            )
        )
        swedishBankIdCredentialRepository.delete(credential)
        val user = userRepository.findByIdOrNull(credential.user.id)
        Assertions.assertThat(user).isNotNull()
    }

    @Test
    fun `deletes the SwedishBankIdCredential when deleting a User`() {
        val user = User(associatedMemberId = "test-member-id")
        val newCredential = swedishBankIdCredentialRepository.saveAndFlush(
            SwedishBankIdCredential(
                personalNumber = "2002022002",
                user = user
            )
        )
        entityManager.refresh(user)
        userRepository.deleteById(user.id)
        val credential = swedishBankIdCredentialRepository.findByIdOrNull(newCredential.id)
        Assertions.assertThat(credential).isNull()
    }
}
