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
internal class ZignSecCredentialTest {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var zignSecCredentialRepository: ZignSecCredentialRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    fun `can create ZignSecCredential for a User with provider name and id number`() {
        val user = userRepository.saveAndFlush(User(associatedMemberId = "test-member-id"))
        val credential = zignSecCredentialRepository.saveAndFlush(
            ZignSecCredential(
                user = user,
                idProviderName = "no-bankid",
                idProviderPersonId = "1234"
            )
        )
        Assertions.assertThat(credential.id).isNotEqualTo(0)
    }

    @Test
    fun `created ZignSecCredential can be retrieved with provider name and id number`() {
        zignSecCredentialRepository.saveAndFlush(
            ZignSecCredential(
                user = User(associatedMemberId = "test-member-id"),
                idProviderName = "no-bankid",
                idProviderPersonId = "1234"
            )
        )
        val credential = zignSecCredentialRepository.findByIdProviderNameAndIdProviderPersonId("no-bankid", "1234")
        Assertions.assertThat(credential).isNotNull
    }

    @Test
    fun `retrieves null if no ZignSecCredential exists with provider name and id number`() {
        val credential = zignSecCredentialRepository.findByIdProviderNameAndIdProviderPersonId("no-bankid", "1234")
        Assertions.assertThat(credential).isNull()
    }

    @Test
    fun `can not create ZignSecCredential with duplicate provider name and id number`() {
        zignSecCredentialRepository.saveAndFlush(
            ZignSecCredential(
                user = User(associatedMemberId = "test-member-id1"),
                idProviderName = "no-bankid",
                idProviderPersonId = "1234"
            )
        )
        assertThrows<DataIntegrityViolationException> {
            zignSecCredentialRepository.saveAndFlush(
                ZignSecCredential(
                    user = User(associatedMemberId = "test-member-id2"),
                    idProviderName = "no-bankid",
                    idProviderPersonId = "1234"
                )
            )
        }
    }

    @Test
    fun `can create ZignSecCredential with same provider id number but different provider name`() {
        zignSecCredentialRepository.saveAndFlush(
            ZignSecCredential(
                user = User(associatedMemberId = "test-member-id1"),
                idProviderName = "no-bankid",
                idProviderPersonId = "1234"
            )
        )
        zignSecCredentialRepository.saveAndFlush(
            ZignSecCredential(
                user = User(associatedMemberId = "test-member-id2"),
                idProviderName = "dk-bankid",
                idProviderPersonId = "1234"
            )
        )
    }

    @Test
    fun `can not create two ZignSecCredentials with same user`() {
        val user = User(associatedMemberId = "test-member-id")
        zignSecCredentialRepository.saveAndFlush(
            ZignSecCredential(
                user = user,
                idProviderName = "no-bankid",
                idProviderPersonId = "1234a"
            )
        )
        assertThrows<DataIntegrityViolationException> {
            zignSecCredentialRepository.saveAndFlush(
                ZignSecCredential(
                    user = user,
                    idProviderName = "no-bankid",
                    idProviderPersonId = "1234b"
                )
            )
        }
    }

    @Test
    fun `can not create ZignSecCredential with duplicate associatedMemberId`() {
        zignSecCredentialRepository.saveAndFlush(
            ZignSecCredential(
                user = User(associatedMemberId = "test-member-id"),
                idProviderName = "no-bankid",
                idProviderPersonId = "1234a"
            )
        )
        assertThrows<DataIntegrityViolationException> {
            zignSecCredentialRepository.saveAndFlush(
                ZignSecCredential(
                    user = User(associatedMemberId = "test-member-id"),
                    idProviderName = "no-bankid",
                    idProviderPersonId = "1234b"
                )
            )
        }
    }

    @Test
    fun `created ZignSecCredential receive a createdAt timestamp`() {
        val before = Instant.now()
        val createdCredential = zignSecCredentialRepository.saveAndFlush(
            ZignSecCredential(
                user = User(associatedMemberId = "test-member-id"),
                idProviderName = "no-bankid",
                idProviderPersonId = "1234"
            )
        )
        val after = Instant.now()
        Assertions.assertThat(createdCredential.createdAt).isAfterOrEqualTo(before)
        Assertions.assertThat(createdCredential.createdAt).isBeforeOrEqualTo(after)
    }

    @Test
    fun `can retrieve associatedMemberId through ZignSecCredential using provider name and id number`() {
        zignSecCredentialRepository.saveAndFlush(
            ZignSecCredential(
                user = User(associatedMemberId = "test-member-id"),
                idProviderName = "no-bankid",
                idProviderPersonId = "1234"
            )
        )
        val credential = zignSecCredentialRepository.findByIdProviderNameAndIdProviderPersonId("no-bankid", "1234")
        Assertions.assertThat(credential).isNotNull
        Assertions.assertThat(credential!!.user.associatedMemberId).isEqualTo("test-member-id")
    }

    @Test
    fun `can delete ZignSecCredential`() {
        val credential = zignSecCredentialRepository.saveAndFlush(
            ZignSecCredential(
                user = User(associatedMemberId = "test-member-id"),
                idProviderName = "no-bankid",
                idProviderPersonId = "1234"
            )
        )
        zignSecCredentialRepository.delete(credential)
        val credentialAfterDeletion = zignSecCredentialRepository.findByIdOrNull(credential.id)
        Assertions.assertThat(credentialAfterDeletion).isNull()
    }

    @Test
    fun `does not delete User when ZignSecCredential is deleted`() {
        val credential = zignSecCredentialRepository.saveAndFlush(
            ZignSecCredential(
                user = User(associatedMemberId = "test-member-id"),
                idProviderName = "no-bankid",
                idProviderPersonId = "1234"
            )
        )
        zignSecCredentialRepository.delete(credential)
        val user = userRepository.findByIdOrNull(credential.user.id)
        Assertions.assertThat(user).isNotNull
    }

    @Test
    fun `deletes the ZignSecCredential when deleting a User`() {
        val user = User(associatedMemberId = "test-member-id")
        val newCredential = zignSecCredentialRepository.saveAndFlush(
            ZignSecCredential(
                user = user,
                idProviderName = "no-bankid",
                idProviderPersonId = "1234"
            )
        )
        entityManager.refresh(user)
        userRepository.deleteById(user.id)
        val credential = zignSecCredentialRepository.findByIdOrNull(newCredential.id)
        Assertions.assertThat(credential).isNull()
    }
}
