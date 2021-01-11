package com.hedvig.memberservice.entities

import com.hedvig.memberservice.services.signing.underwriter.strategy.SignStrategy
import java.util.*
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(uniqueConstraints=[UniqueConstraint(columnNames = ["underwriterSignSessionReference", "signReference"])])
class UnderwriterSignSessionEntity(
    val underwriterSignSessionReference: UUID,
    val signReference: UUID,
    val memberId: Long?,
    @Enumerated(EnumType.STRING)
    val signStrategy: SignStrategy?
) {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val internalId: Long = 0

    constructor() : this(UUID.randomUUID(), UUID.randomUUID(), null, null)
}
