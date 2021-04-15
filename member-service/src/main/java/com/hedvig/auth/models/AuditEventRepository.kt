package com.hedvig.auth.models

import org.springframework.data.jpa.repository.JpaRepository

internal interface AuditEventRepository: JpaRepository<AuditEvent, Long> {
}
