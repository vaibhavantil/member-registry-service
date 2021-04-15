package com.hedvig.auth.models

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
internal interface SwedishBankIdCredentialRepository : JpaRepository<SwedishBankIdCredential, Long> {
    fun findByPersonalNumber(personalNumber: String): SwedishBankIdCredential?
}
