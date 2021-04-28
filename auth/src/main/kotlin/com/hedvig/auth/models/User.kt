package com.hedvig.auth.models

import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID
import javax.persistence.*

/**
 * String representation of a member ID.
 */
typealias MemberId = String

/**
 * A user is a data subject that can authenticate and interact with the Hedvig system. A user is
 * associated with a specific member, but that member does not necessarily need to be a member with
 * a signed contract.
 */
@Entity
@Table(name = "user_entity")
class User(
    @Column(unique = true)
    val associatedMemberId: MemberId
) {
    @Id
    val id: UUID = UUID.randomUUID()

    @field:CreationTimestamp
    lateinit var createdAt: Instant

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val auditLog: List<AuditEvent> = listOf()

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    internal var swedishBankIdCredential: SwedishBankIdCredential? = null

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    internal var zignSecCredential: ZignSecCredential? = null

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    internal var simpleSignConnection: SimpleSignConnection? = null
}
