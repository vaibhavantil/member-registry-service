package com.hedvig.memberservice.identity.models

import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Index
import javax.persistence.Table
import org.hibernate.annotations.CreationTimestamp

@Entity
@Table(
    indexes = [Index(columnList = "member_id")]
)
class MemberIdentityRevision(
    @Column(name = "member_id")
    val memberId: Long,
    val firstName: String?,
    val lastName: String?,
    val nationalIdentifier: String,
    val countryCode: String,
    val identificationSource: String
) {
    @Id @GeneratedValue
    val id: Long = 0

    @field:CreationTimestamp
    lateinit var createdAt: Instant
}
