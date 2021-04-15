package com.hedvig.auth.models

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
internal class SimpleSignConnectionTest {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var simpleSignConnectionRepository: SimpleSignConnectionRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

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
