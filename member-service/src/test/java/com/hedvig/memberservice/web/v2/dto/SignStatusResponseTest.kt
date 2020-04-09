package com.hedvig.memberservice.web.v2.dto

import com.hedvig.memberservice.entities.SignSession
import com.hedvig.memberservice.entities.SignStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SignStatusResponseTest {

    @Test
    fun inProgressIfContractIsNotCreated() {
        val session = SignSession()
        session.hasContract = false
        session.status = SignStatus.COMPLETED

        val response = SignStatusResponse.CreateFromEntity(session)

        assertThat(response.status).isEqualTo(SignStatus.IN_PROGRESS)
    }

    @Test
    fun completedIfStatusIsCompleteContractIsCreated() {
        val session = SignSession()
        session.hasContract = true
        session.status = SignStatus.COMPLETED

        val response = SignStatusResponse.CreateFromEntity(session)

        assertThat(response.status).isEqualTo(SignStatus.COMPLETED)
    }
}
