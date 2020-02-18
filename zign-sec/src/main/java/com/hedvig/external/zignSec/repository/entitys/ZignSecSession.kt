package com.hedvig.external.zignSec.repository.entitys

import com.hedvig.external.authentication.dto.NorwegianBankIdProgressStatus
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.*
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Enumerated
import javax.persistence.Id

@Entity
class ZignSecSession(
    @Id
    val sessionId: UUID = UUID.randomUUID(),
    @Column(unique = true)
    val memberId: Long,
    @Enumerated(javax.persistence.EnumType.STRING)
    var status: NorwegianBankIdProgressStatus = NorwegianBankIdProgressStatus.INITIATED,
    @Enumerated(javax.persistence.EnumType.STRING)
    val requestType: NorwegianAuthenticationType,
    @Embedded
    var notification: ZignSecNotification? = null,
    @CreationTimestamp
    val createdAt: Instant = Instant.now(),
    @UpdateTimestamp
    var updatedAt: Instant = Instant.now()
) {
    constructor() : this(
        memberId = 1337,
        requestType = NorwegianAuthenticationType.AUTH
    )
}

