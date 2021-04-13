package com.hedvig.auth

import com.hedvig.auth.model.SimpleSignConnection
import com.hedvig.auth.model.SimpleSignConnectionRepository
import com.hedvig.auth.model.SwedishBankIdCredential
import com.hedvig.auth.model.SwedishBankIdCredentialRepository
import com.hedvig.auth.model.User
import com.hedvig.auth.model.UserRepository
import com.hedvig.auth.model.ZignSecCredential
import com.hedvig.auth.model.ZignSecCredentialRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant

@DataJpaTest
internal class AuthTest {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var swedishBankIdCredentialRepository: SwedishBankIdCredentialRepository

    @Autowired
    lateinit var zignSecCredentialRepository: ZignSecCredentialRepository

    @Autowired
    lateinit var simpleSignConnectionRepository: SimpleSignConnectionRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    fun `can create a User`() {
        val newUser = User(associatedMemberId = "test-member-id")
        userRepository.saveAndFlush(newUser)
    }

    @Test
    fun `can delete a User`() {
        val newUser = User(associatedMemberId = "test-member-id")
        userRepository.saveAndFlush(newUser)
        userRepository.delete(newUser)
        val user = userRepository.findByIdOrNull(newUser.id)
        assertThat(user).isNull()
    }

    @Test
    fun `can retrieve an existing User`() {
        val newUser = User(associatedMemberId = "test-member-id")
        userRepository.saveAndFlush(newUser)

        val sameUser = userRepository.findByIdOrNull(newUser.id)
        assertThat(sameUser).isNotNull
    }

    @Test
    fun `cannot create two Users with the same associatedMemberId`() {
        userRepository.saveAndFlush(
            User(associatedMemberId = "test-member-id")
        )
        assertThrows<DataIntegrityViolationException> {
            userRepository.saveAndFlush(
                User(associatedMemberId = "test-member-id")
            )
        }
    }

    @Test
    fun `created Users receive a createdAt timestamp`() {
        val before = Instant.now()
        val createdUser = userRepository.saveAndFlush(
            User(associatedMemberId = "test-member-id")
        )
        val after = Instant.now()
        assertThat(createdUser.createdAt).isAfterOrEqualTo(before)
        assertThat(createdUser.createdAt).isBeforeOrEqualTo(after)
    }

    @Test
    fun `can create SwedishBankIdCredential for a User with personalNumber`() {
        val user = userRepository.saveAndFlush(User(associatedMemberId = "test-member-id"))
        val credential = swedishBankIdCredentialRepository.saveAndFlush(
            SwedishBankIdCredential(
                personalNumber = "2002022002",
                user = user
            )
        )
        assertThat(credential.id).isNotEqualTo(0)
    }

    @Test
    fun `can get SwedishBankIdCredential for a User`() {
        val user = User(associatedMemberId = "test-member-id")
        val credential = SwedishBankIdCredential(
            personalNumber = "2002022002",
            user = user
        )
        swedishBankIdCredentialRepository.saveAndFlush(credential)
        entityManager.refresh(user)
        assertThat(user.swedishBankIdCredential).isNotNull()
        assertThat(user.swedishBankIdCredential!!.id).isEqualTo(credential.id)
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
        assertThat(credential).isNotNull()
    }

    @Test
    fun `retrieves null if no SwedishBankIdCredential exists by personalNumber`() {
        val credential = swedishBankIdCredentialRepository.findByPersonalNumber("2002022002")
        assertThat(credential).isNull()
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
        assertThat(createdCredential.createdAt).isAfterOrEqualTo(before)
        assertThat(createdCredential.createdAt).isBeforeOrEqualTo(after)
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
        assertThat(credential).isNotNull()
        assertThat(credential!!.user.associatedMemberId).isEqualTo("test-member-id")
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
        assertThat(credentialAfterDeletion).isNull()
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
        assertThat(user).isNotNull()
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
        assertThat(credential).isNull()
    }

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
        assertThat(credential.id).isNotEqualTo(0)
    }

    @Test
    fun `can get ZignSecCredential for a User`() {
        val user = User(associatedMemberId = "test-member-id")
        val credential = ZignSecCredential(
            user = user,
            idProviderName = "no-bankid",
            idProviderPersonId = "1234"
        )
        zignSecCredentialRepository.saveAndFlush(credential)
        entityManager.refresh(user)
        assertThat(user.zignSecCredential).isNotNull
        assertThat(user.zignSecCredential!!.id).isEqualTo(credential.id)
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
        assertThat(credential).isNotNull
    }

    @Test
    fun `retrieves null if no ZignSecCredential exists with provider name and id number`() {
        val credential = zignSecCredentialRepository.findByIdProviderNameAndIdProviderPersonId("no-bankid", "1234")
        assertThat(credential).isNull()
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
        assertThat(createdCredential.createdAt).isAfterOrEqualTo(before)
        assertThat(createdCredential.createdAt).isBeforeOrEqualTo(after)
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
        assertThat(credential).isNotNull
        assertThat(credential!!.user.associatedMemberId).isEqualTo("test-member-id")
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
        assertThat(credentialAfterDeletion).isNull()
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
        assertThat(user).isNotNull
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
        assertThat(credential).isNull()
    }

    @Test
    fun `can create SimpleSignConnection for a User with personal number and country`() {
        val credential = simpleSignConnectionRepository.saveAndFlush(
            SimpleSignConnection(
                user = User(associatedMemberId = "test-member-id"),
                personalNumber = "1234",
                country = "NO"
            )
        )
        assertThat(credential.id).isNotEqualTo(0)
    }

    @Test
    fun `can get SimpleSignConnection for a User`() {
        val user = User(associatedMemberId = "test-member-id")
        val connection = SimpleSignConnection(
            user = user,
            personalNumber = "1234",
            country = "NO"
        )
        simpleSignConnectionRepository.saveAndFlush(connection)
        entityManager.refresh(user)
        assertThat(user.simpleSignConnection).isNotNull
        assertThat(user.simpleSignConnection!!.id).isEqualTo(connection.id)
    }

    @Test
    fun `created SimpleSignConnection can be retrieved with personal number and country`() {
        simpleSignConnectionRepository.saveAndFlush(
            SimpleSignConnection(
                user = User(associatedMemberId = "test-member-id"),
                personalNumber = "1234",
                country = "NO"
            )
        )
        val connection = simpleSignConnectionRepository.findByPersonalNumberAndCountry("1234", "NO")
        assertThat(connection).isNotNull
    }

    @Test
    fun `retrieves null if no SimpleSignConnection exists with personal number and country`() {
        val connection = simpleSignConnectionRepository.findByPersonalNumberAndCountry("1234", "NO")
        assertThat(connection).isNull()
    }

    @Test
    fun `can not create SimpleSignConnection with duplicate personal number and country`() {
        simpleSignConnectionRepository.saveAndFlush(
            SimpleSignConnection(
                user = User(associatedMemberId = "test-member-id1"),
                personalNumber = "1234",
                country = "NO"
            )
        )
        assertThrows<DataIntegrityViolationException> {
            simpleSignConnectionRepository.saveAndFlush(
                SimpleSignConnection(
                    user = User(associatedMemberId = "test-member-id2"),
                    personalNumber = "1234",
                    country = "NO"
                )
            )
        }
    }

    @Test
    fun `can create SimpleSignConnection with same personal number but different country`() {
        simpleSignConnectionRepository.saveAndFlush(
            SimpleSignConnection(
                user = User(associatedMemberId = "test-member-id1"),
                personalNumber = "1234",
                country = "NO"
            )
        )
        simpleSignConnectionRepository.saveAndFlush(
            SimpleSignConnection(
                user = User(associatedMemberId = "test-member-id2"),
                personalNumber = "1234",
                country = "DK"
            )
        )
    }

    @Test
    fun `can not create two SimpleSignConnection with same user`() {
        val user = User(associatedMemberId = "test-member-id")
        simpleSignConnectionRepository.saveAndFlush(
            SimpleSignConnection(
                user = user,
                personalNumber = "1234",
                country = "DK"
            )
        )
        assertThrows<DataIntegrityViolationException> {
            simpleSignConnectionRepository.saveAndFlush(
                SimpleSignConnection(
                    user = user,
                    personalNumber = "1234",
                    country = "DK"
                )
            )
        }
    }

    @Test
    fun `created SimpleSignConnection receive a createdAt timestamp`() {
        val before = Instant.now()
        val createdConnection = simpleSignConnectionRepository.saveAndFlush(
            SimpleSignConnection(
                user = User(associatedMemberId = "test-member-id"),
                personalNumber = "1234",
                country = "NO"
            )
        )
        val after = Instant.now()
        assertThat(createdConnection.createdAt).isAfterOrEqualTo(before)
        assertThat(createdConnection.createdAt).isBeforeOrEqualTo(after)
    }

    @Test
    fun `can retrieve associatedMemberId through SimpleSignConnection personal number and country`() {
        simpleSignConnectionRepository.saveAndFlush(
            SimpleSignConnection(
                user = User(associatedMemberId = "test-member-id"),
                personalNumber = "1234",
                country = "NO"
            )
        )
        val connection = simpleSignConnectionRepository.findByPersonalNumberAndCountry("1234", "NO")
        assertThat(connection).isNotNull
        assertThat(connection!!.user.associatedMemberId).isEqualTo("test-member-id")
    }

    @Test
    fun `can delete SimpleSignConnection`() {
        val connection = simpleSignConnectionRepository.saveAndFlush(
            SimpleSignConnection(
                user = User(associatedMemberId = "test-member-id"),
                personalNumber = "1234",
                country = "NO"
            )
        )
        simpleSignConnectionRepository.delete(connection)
        val connectionAfterDeletion = simpleSignConnectionRepository.findByIdOrNull(connection.id)
        assertThat(connectionAfterDeletion).isNull()
    }

    @Test
    fun `does not delete User when SimpleSignConnection is deleted`() {
        val connection = simpleSignConnectionRepository.saveAndFlush(
            SimpleSignConnection(
                user = User(associatedMemberId = "test-member-id"),
                personalNumber = "1234",
                country = "NO"
            )
        )
        simpleSignConnectionRepository.delete(connection)
        val user = userRepository.findByIdOrNull(connection.user.id)
        assertThat(user).isNotNull
    }

    @Test
    fun `deletes the SimpleSignConnection when deleting a User`() {
        val user = User(associatedMemberId = "test-member-id")
        val newConnection = simpleSignConnectionRepository.saveAndFlush(
            SimpleSignConnection(
                user = user,
                personalNumber = "1234",
                country = "NO"
            )
        )
        entityManager.refresh(user)
        userRepository.deleteById(user.id)
        val credential = simpleSignConnectionRepository.findByIdOrNull(newConnection.id)
        assertThat(credential).isNull()
    }
}
