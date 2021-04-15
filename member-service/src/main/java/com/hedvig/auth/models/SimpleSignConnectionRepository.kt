package com.hedvig.auth.models

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
internal interface SimpleSignConnectionRepository : JpaRepository<SimpleSignConnection, Long> {
    fun findByPersonalNumberAndCountry(personalNumber: String, country: String): SimpleSignConnection?
}
