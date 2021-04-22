package com.hedvig.auth.models

import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import javax.persistence.*

@Entity
internal class AuditEvent(
    @ManyToOne
    val user: User,

    @Enumerated(EnumType.STRING)
    val eventType: EventType
) {
    @Id
    @GeneratedValue
    val id: Long = 0

    @field:CreationTimestamp
    lateinit var timestamp: Instant

    enum class EventType {
        CREATED_ON_IMPORT,
        CREATED_ON_LOGIN
    }
}
