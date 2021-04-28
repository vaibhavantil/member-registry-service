package com.hedvig.auth.models

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant

@DataJpaTest
internal class UserTest {

    @Autowired
    lateinit var userRepository: UserRepository

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
        Assertions.assertThat(user).isNull()
    }

    @Test
    fun `can retrieve an existing User`() {
        val newUser = User(associatedMemberId = "test-member-id")
        userRepository.saveAndFlush(newUser)

        val sameUser = userRepository.findByIdOrNull(newUser.id)
        Assertions.assertThat(sameUser).isNotNull
    }

    @Test
    fun `can not create two Users with the same associatedMemberId`() {
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
        Assertions.assertThat(createdUser.createdAt).isAfterOrEqualTo(before)
        Assertions.assertThat(createdUser.createdAt).isBeforeOrEqualTo(after)
    }
}
