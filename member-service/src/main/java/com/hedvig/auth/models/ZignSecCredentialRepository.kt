package com.hedvig.auth.models

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
internal interface ZignSecCredentialRepository : JpaRepository<ZignSecCredential, Long> {
    fun findByIdProviderNameAndIdProviderPersonId(idProviderName: String, idProviderPersonId: String): ZignSecCredential?
}
