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
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(columnNames = ["personal_number", "country"])]
)
internal class SimpleSignConnection(
    @OneToOne(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH])
    @JoinColumn(unique = true)
    val user: User,
    @Column(name = "personal_number")
    val personalNumber: String,
    @Column(name = "country")
    val country: String
) {
    @Id
    @GeneratedValue
    val id: Long = 0

    @field:CreationTimestamp
    lateinit var createdAt: Instant
}
