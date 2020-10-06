package com.hedvig.external.zignSec.repository.entitys

import com.hedvig.external.authentication.dto.ZignSecAuthenticationMethod
import com.hedvig.external.authentication.dto.ZignSecBankIdProgressStatus
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.*
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class ZignSecSession(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val sessionId: Long = 0,
    @Column(unique = true)
    val memberId: Long,
    @Column(unique = true)
    var referenceId: UUID,
    @Column(length=10_000)
    var redirectUrl: String,
    @Enumerated(javax.persistence.EnumType.STRING)
    var status: ZignSecBankIdProgressStatus = ZignSecBankIdProgressStatus.INITIATED,
    @Enumerated(javax.persistence.EnumType.STRING)
    val requestType: ZignSecAuthenticationType,
    @Column(columnDefinition = "varchar(11) default null", nullable = true)
    var requestPersonalNumber: String? = null,
    @Embedded
    var notification: ZignSecNotification? = null,
    @Column(columnDefinition = "boolean default false")
    var isContractsCreated: Boolean = false,
    @CreationTimestamp
    val createdAt: Instant = Instant.now(),
    @UpdateTimestamp
    var updatedAt: Instant = Instant.now(),
    @Column(columnDefinition = "varchar(30) default 'NORWAY_WEB_OR_MOBILE'", nullable = false)
    @Enumerated(javax.persistence.EnumType.STRING)
    var authenticationMethod: ZignSecAuthenticationMethod
) {
    constructor() : this(
        memberId = 1337,
        requestType = ZignSecAuthenticationType.AUTH,
        referenceId = UUID.randomUUID(),
        redirectUrl = "empty constructor",
        requestPersonalNumber = null,
        authenticationMethod = ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
    )
}

