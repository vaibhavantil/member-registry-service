package com.hedvig.memberservice.identity.repository

import org.springframework.data.jpa.repository.JpaRepository

interface IdentityRepository : JpaRepository<IdentityEntity, Long>
