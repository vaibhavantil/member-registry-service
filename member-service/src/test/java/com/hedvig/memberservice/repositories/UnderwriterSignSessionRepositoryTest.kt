package com.hedvig.memberservice.repositories

import com.hedvig.memberservice.entities.UnderwriterSignSessionEntity
import com.hedvig.memberservice.query.UnderwriterSignSessionRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@DataJpaTest
@RunWith(SpringRunner::class)
class UnderwriterSignSessionRepositoryTest {

    @Autowired
    lateinit var underwriterSignSessionRepository: UnderwriterSignSessionRepository

    @Test
    fun storeAndFindUnderwriterSignSessionWithSingReference() {
        val sessionReference = UUID.randomUUID()
        val signReference = UUID.randomUUID()

        underwriterSignSessionRepository.save(UnderwriterSignSessionEntity(sessionReference, signReference, null, null))

        val session = underwriterSignSessionRepository.findBySignReference(signReference)

        assertThat(session?.underwriterSignSessionReference).isEqualTo(sessionReference)
    }
}
