package com.hedvig.auth.model

import java.time.Instant
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(columnNames = ["id_provider_name", "id_provider_person_id"])]
)
class ZignSecCredential(
    @OneToOne(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH])
    @JoinColumn(unique = true)
    val user: User,
    @Column(name = "id_provider_name")
    val idProviderName: String,
    @Column(name = "id_provider_person_id")
    val idProviderPersonId: String,
    val createdAt: Instant = Instant.now()
) {
    @Id
    @GeneratedValue
    val id: Long = 0
}
