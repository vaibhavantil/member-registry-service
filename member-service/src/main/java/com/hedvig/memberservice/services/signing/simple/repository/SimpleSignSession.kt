package com.hedvig.memberservice.services.signing.simple.repository

import com.hedvig.memberservice.web.dto.Nationality
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
    val nationalIdentification: String,
    val nationality: Nationality,
    var isContractsCreated: Boolean = false
)
