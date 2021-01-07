package com.hedvig.memberservice.web.dto

import com.hedvig.memberservice.query.MemberEntity
import com.hedvig.memberservice.util.Gender
import com.hedvig.memberservice.util.SsnUtilImpl.Companion.instance
import java.time.Instant
import java.time.LocalDate
import java.util.ArrayList

class InternalMember(
    val memberId: Long? = null,
    val status: String? = null,
    val ssn: String? = null,
    val gender: Gender? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val street: String? = null,
    val floor: Int? = null,
    val apartment: String? = null,
    val city: String? = null,
    val zipCode: String? = null,
    val country: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val birthDate: LocalDate? = null,
    val signedOn: Instant? = null,
    val createdOn: Instant? = null,
    val fraudulentStatus: String? = null,
    val fraudulentDescription: String? = null,
    val acceptLanguage: String? = null,
    val traceMemberInfo: List<TraceMemberDTO> = ArrayList()
) {

    companion object {
        fun fromEntity(entity: MemberEntity) = InternalMember(
            entity.getId(),
            if (entity.getStatus() != null) entity.getStatus().name else "",
            entity.getSsn(),
            instance.getGenderFromSsn(entity.getSsn()),
            entity.getFirstName(),
            entity.getLastName(),
            entity.getStreet(),
            entity.getFloor(),
            entity.getApartment(),
            entity.getCity(),
            entity.zipCode,
            "SE",
            entity.getEmail(),
            entity.getPhoneNumber(),
            entity.getBirthDate(),
            entity.getSignedOn(),
            entity.getCreatedOn(),
            if (entity.getFraudulentStatus() != null) entity.getFraudulentStatus().name else "",
            entity.getFraudulentDescription(),
            entity.getAcceptLanguage()
        )
    }
}
