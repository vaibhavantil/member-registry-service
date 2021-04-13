package com.hedvig.auth.model

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SimpleSignConnectionRepository : JpaRepository<SimpleSignConnection, Long> {
    fun findByPersonalNumberAndCountry(personalNumber: String, country: String): SimpleSignConnection?
}
