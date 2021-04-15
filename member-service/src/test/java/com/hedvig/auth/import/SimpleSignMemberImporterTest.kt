package com.hedvig.auth.import

import com.hedvig.auth.models.UserRepository
import com.hedvig.memberservice.events.MemberSimpleSignedEvent
import java.time.Instant
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.ApplicationContext
import javax.persistence.EntityManager

@DataJpaTest
internal class SimpleSignMemberImporterTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val entityManager: TestEntityManager
) {

    private lateinit var importer: SimpleSignMemberImporter

    @BeforeEach
    fun setup(@Autowired context: ApplicationContext) {
        importer = context.autowireCapableBeanFactory.createBean(SimpleSignMemberImporter::class.java)
    }

    @Test
    fun `signed member is imported on event`() {
        val memberId = 123L
        val personalNumber = "01129955131"

        importer.on(
            MemberSimpleSignedEvent(memberId, personalNumber, MemberSimpleSignedEvent.Nationality.NORWAY, UUID.randomUUID()),
            Instant.now()
        )

        val user = userRepository.findByAssociatedMemberId(memberId.toString())
        assertThat(user).isNotNull
        assertThat(user?.simpleSignConnection?.personalNumber).isEqualTo(personalNumber)
        assertThat(user?.simpleSignConnection?.country).isEqualTo("NO")
    }

    @Test
    fun `imported member receives correct timestamp`() {
        val memberId = 123L
        val personalNumber = "201212121212"
        val time = Instant.now().minusSeconds(1000)

        importer.on(
            MemberSimpleSignedEvent(memberId, personalNumber, MemberSimpleSignedEvent.Nationality.NORWAY, UUID.randomUUID()),
            time
        )

        val user = userRepository.findByAssociatedMemberId(memberId.toString())
        assertThat(user?.createdAt).isEqualTo(time)
        assertThat(user?.simpleSignConnection?.createdAt).isEqualTo(time)
    }

    @Test
    fun `signed member is simply ignored second attempt`() {
        val memberId = 123L
        val personalNumber = "01129955131"

        importer.on(
            MemberSimpleSignedEvent(memberId, personalNumber, MemberSimpleSignedEvent.Nationality.NORWAY, UUID.randomUUID()),
            Instant.now()
        )
        entityManager.clear()
        importer.on(
            MemberSimpleSignedEvent(memberId, personalNumber, MemberSimpleSignedEvent.Nationality.NORWAY, UUID.randomUUID()),
            Instant.now()
        )

        val users = userRepository.findAll()
        assertThat(users.size).isEqualTo(1)
    }

    @Test
    fun `signing second member with same personal number and country again should replace the corresponding user`() {
        val memberId1 = 123L
        val memberId2 = 456L
        val personalNumber = "01129955131"

        importer.on(
            MemberSimpleSignedEvent(memberId1, personalNumber, MemberSimpleSignedEvent.Nationality.NORWAY, UUID.randomUUID()),
            Instant.now()
        )
        entityManager.clear()
        importer.on(
            MemberSimpleSignedEvent(memberId2, personalNumber, MemberSimpleSignedEvent.Nationality.NORWAY, UUID.randomUUID()),
            Instant.now()
        )

        assertThat(userRepository.findByAssociatedMemberId(memberId1.toString())).isNull()
        assertThat(userRepository.findByAssociatedMemberId(memberId2.toString())).isNotNull
    }
}
