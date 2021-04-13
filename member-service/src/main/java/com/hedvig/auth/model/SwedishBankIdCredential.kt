package com.hedvig.auth.model

import java.time.Instant
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne

@Entity
class SwedishBankIdCredential(
    @OneToOne(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH])
    @JoinColumn(unique = true)
    val user: User,
    @Column(unique = true)
    val personalNumber: String,
    val createdAt: Instant = Instant.now()
) {
    @Id @GeneratedValue
    val id: Long = 0
}
