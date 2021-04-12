package com.hedvig.auth.model

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ZignSecCredentialRepository : JpaRepository<ZignSecCredential, Long> {
    fun findByIdProviderNameAndIdProviderPersonId(idProviderName: String, idProviderPersonId: String): ZignSecCredential?
}
