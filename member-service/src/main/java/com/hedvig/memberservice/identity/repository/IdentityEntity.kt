package com.hedvig.memberservice.identity.repository

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id

@Entity
class IdentityEntity(
    @Id
    val memberId: Long,
    @Embedded
    val nationalIdentification: NationalIdentification,
    @Enumerated(EnumType.STRING)
    val identificationMethod: IdentificationMethod,
    val firstName: String?,
    val lastName: String?
  ) {
  @field:CreationTimestamp
  @Column(updatable = false)
  lateinit var createdAt: Instant
  @field:UpdateTimestamp
  lateinit var updatedAt: Instant
    fun update(
        identityEntity: IdentityEntity
    ) = IdentityEntity(
        this.memberId,
        identityEntity.nationalIdentification,
        identityEntity.identificationMethod,
        identityEntity.firstName ?: this.firstName,
        identityEntity.lastName ?: this.lastName,
        this.createdAt,
        Instant.now()
    )
}
