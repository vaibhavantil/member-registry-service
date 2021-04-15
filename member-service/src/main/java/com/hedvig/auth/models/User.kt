package com.hedvig.auth.models

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import java.time.Instant
import java.util.UUID
import javax.persistence.*

typealias MemberId = String

@Entity
@Table(name = "user_entity")
class User(
    @Column(unique = true)
    val associatedMemberId: MemberId,
    val createdAt: Instant = Instant.now()
) {
    @Id
    val id: UUID = UUID.randomUUID()

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val auditLog: List<AuditEvent> = listOf()

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    internal var swedishBankIdCredential: SwedishBankIdCredential? = null

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    internal var zignSecCredential: ZignSecCredential? = null

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    internal var simpleSignConnection: SimpleSignConnection? = null
}
