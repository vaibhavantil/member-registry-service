package com.hedvig.external.zignSec.repository.entitys

import com.hedvig.external.zignSec.client.dto.ZignSecIdentity
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Embeddable

@Embeddable
class Identity(
    val countryCode: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val fullName: String? = null,
    val personalNumber: String? = null,
    val dateOfBirth: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val idProviderName: String? = null,
    val identificationDate: LocalDateTime? = null,
    val idProviderRequestId: String? = null,
    val idProviderPersonId: String? = null,
    val customerPersonId: String? = null
) {
    companion object {
        fun from(identity: ZignSecIdentity) = Identity(
            identity.countryCode,
            identity.firstName,
            identity.lastName,
            identity.fullName,
            identity.personalNumber,
            identity.dateOfBirth,
            identity.age,
            identity.gender,
            identity.idProviderName,
            identity.identificationDate,
            identity.idProviderRequestId,
            identity.idProviderPersonId,
            identity.customerPersonId
        )
    }
}
