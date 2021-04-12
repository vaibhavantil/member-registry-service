package com.hedvig.auth.model

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SwedishBankIdCredentialRepository : JpaRepository<SwedishBankIdCredential, Long> {
    fun findByPersonalNumber(personalNumber: String): SwedishBankIdCredential?
}
