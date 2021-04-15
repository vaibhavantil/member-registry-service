package com.hedvig.external.zignSec.repository.entitys

import com.hedvig.external.zignSec.client.dto.ZignSecIdentity
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Identity

        if (countryCode != other.countryCode) return false
        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (fullName != other.fullName) return false
        if (personalNumber != other.personalNumber) return false
        if (dateOfBirth != other.dateOfBirth) return false
        if (age != other.age) return false
        if (gender != other.gender) return false
        if (idProviderName != other.idProviderName) return false
        if (identificationDate != other.identificationDate) return false
        if (idProviderRequestId != other.idProviderRequestId) return false
        if (idProviderPersonId != other.idProviderPersonId) return false
        if (customerPersonId != other.customerPersonId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = countryCode?.hashCode() ?: 0
        result = 31 * result + (firstName?.hashCode() ?: 0)
        result = 31 * result + (lastName?.hashCode() ?: 0)
        result = 31 * result + (fullName?.hashCode() ?: 0)
        result = 31 * result + (personalNumber?.hashCode() ?: 0)
        result = 31 * result + (dateOfBirth?.hashCode() ?: 0)
        result = 31 * result + (age ?: 0)
        result = 31 * result + (gender?.hashCode() ?: 0)
        result = 31 * result + (idProviderName?.hashCode() ?: 0)
        result = 31 * result + (identificationDate?.hashCode() ?: 0)
        result = 31 * result + (idProviderRequestId?.hashCode() ?: 0)
        result = 31 * result + (idProviderPersonId?.hashCode() ?: 0)
        result = 31 * result + (customerPersonId?.hashCode() ?: 0)
        return result
    }

}
