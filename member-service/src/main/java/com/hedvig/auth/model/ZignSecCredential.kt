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
import org.hibernate.annotations.CreationTimestamp

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
    val idProviderPersonId: String
) {
    @Id
    @GeneratedValue
    val id: Long = 0

    @field:CreationTimestamp
    @Column(updatable = false)
    lateinit var createdAt: Instant
}
