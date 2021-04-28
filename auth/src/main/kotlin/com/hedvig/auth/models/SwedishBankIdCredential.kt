package com.hedvig.auth.models

import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne

@Entity
internal class SwedishBankIdCredential(
    @OneToOne(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH])
    @JoinColumn(unique = true)
    val user: User,
    @Column(unique = true)
    val personalNumber: String
) {
    @Id
    @GeneratedValue
    val id: Long = 0

    @field:CreationTimestamp
    lateinit var createdAt: Instant
}
