package com.hedvig.auth.model

import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "user_entity")
class User(
    @Column(unique = true)
    val associatedMemberId: String
) {
    @Id
    val id: UUID = UUID.randomUUID()

    @field:CreationTimestamp
    @Column(updatable = false)
    lateinit var createdAt: Instant

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var swedishBankIdCredential: SwedishBankIdCredential? = null

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var zignSecCredential: ZignSecCredential? = null
}
