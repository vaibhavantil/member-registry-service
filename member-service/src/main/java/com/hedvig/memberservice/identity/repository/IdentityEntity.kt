package com.hedvig.memberservice.identity.repository

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import javax.persistence.Embeddable
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id

interface IdentityRepository : JpaRepository<IdentityEntity, Long>

@Entity
class IdentityEntity(
    @Id
    val memberId: Long,
    @Embedded
    val nationalIdentification: NationalIdentification,
    val identificationMethod: IdentificationMethod,
    val fullName: String?,
    @CreationTimestamp
    val createdAt: Instant = Instant.now(),
    @UpdateTimestamp
    var updatedAt: Instant = Instant.now()
) {
    fun update(
        identityEntity: IdentityEntity
    ) = IdentityEntity(
        this.memberId,
        identityEntity.nationalIdentification,
        identityEntity.identificationMethod,
        identityEntity.fullName,
        this.createdAt,
        Instant.now()
    )
}

@Embeddable
class NationalIdentification(
    val identification: String,
    @Enumerated(EnumType.STRING)
    val nationality: Nationality
)

enum class Nationality {
    SWEDEN,
    NORWAY,
    DENMARK
}

enum class IdentificationMethod {
    NORWEGIAN_BANK_ID,
    DANISH_BANK_ID
}
