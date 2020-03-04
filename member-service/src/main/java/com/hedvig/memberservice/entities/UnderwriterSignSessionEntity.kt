package com.hedvig.memberservice.entities

import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(uniqueConstraints=[UniqueConstraint(columnNames = ["signReference"])])
class UnderwriterSignSessionEntity(
    @Id
    val underwriterSignSessionReference: UUID,
    val signReference: UUID
) {
    constructor() : this(UUID.randomUUID(), UUID.randomUUID())
}
