package com.hedvig.auth.models

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

// disambiguate from existing auditEventRepository in spring
@Repository(value = "com.hedvig.auth.models.AuditEventRepository")
internal interface AuditEventRepository : JpaRepository<AuditEvent, Long> {
}
