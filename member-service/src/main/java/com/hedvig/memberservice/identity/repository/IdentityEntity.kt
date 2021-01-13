package com.hedvig.memberservice.identity.repository

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import javax.persistence.Column
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
    @CreationTimestamp
    @Column(updatable = false)
    lateinit var createdAt: Instant

    @UpdateTimestamp
    lateinit var updatedAt: Instant

    fun update(
        identityEntity: IdentityEntity
    ) = IdentityEntity(
        this.memberId,
        identityEntity.nationalIdentification,
        identityEntity.identificationMethod,
        identityEntity.firstName ?: this.firstName,
        identityEntity.lastName ?: this.lastName
    )

    fun hasNewOrMoreNewInfo(newIdentityEntity: IdentityEntity): Boolean {
        if (this.memberId != newIdentityEntity.memberId) {
            throw IllegalCallerException("hasNewOrMoreNewInfo should not be called with entities that has different member id")
        }

        if (
            this.nationalIdentification == newIdentityEntity.nationalIdentification ||
            this.identificationMethod == newIdentityEntity.identificationMethod ||
            this.firstName == newIdentityEntity.firstName ||
            this.lastName == newIdentityEntity.lastName
        ) {
            return false
        }

        return true
    }
}
