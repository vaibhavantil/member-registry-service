package com.hedvig.external.zignSec.repository.entitys

import com.hedvig.external.zignSec.client.dto.ZignSecNotificationRequest
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Embeddable
import javax.persistence.OneToMany

@Embeddable
class ZignSecNotification(
    @ElementCollection
    val errors: List<Error>,
    val identity: Identity?,
    val method: String?,
    @Column(columnDefinition="VARCHAR(10000)")
    val bankIdNoOidc: String?
) {
    constructor() : this(
        errors = emptyList(),
        identity = null,
        method = null,
        bankIdNoOidc = null
    )

    companion object {
        fun from(request: ZignSecNotificationRequest)= ZignSecNotification(
            request.errors.map { Error(it.code, it.description) },
            request.identity?.let { Identity.from(it) },
            request.method,
            request.bankIdNoOidc
        )
    }
}
