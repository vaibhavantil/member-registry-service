package com.hedvig.memberservice.services.signing.simple.repository

import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class SimpleSignSession(
    @Id
    val referenceId: UUID,
    @Column(unique = true)
    val memberId: Long,
    var isContractsCreated: Boolean = false
)
