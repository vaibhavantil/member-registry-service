package com.hedvig.memberservice.entities

import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(uniqueConstraints=[UniqueConstraint(columnNames = ["underwriterSignSessionReference", "signReference"])])
class UnderwriterSignSessionEntity(
    val underwriterSignSessionReference: UUID,
    val signReference: UUID
) {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val internalId: Long = 0

    constructor() : this(UUID.randomUUID(), UUID.randomUUID())
}
