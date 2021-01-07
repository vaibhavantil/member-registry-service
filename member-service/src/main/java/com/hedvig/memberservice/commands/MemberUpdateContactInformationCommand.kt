package com.hedvig.memberservice.commands

import com.hedvig.memberservice.web.dto.UpdateContactInformationRequest
import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.time.LocalDate

class MemberUpdateContactInformationCommand(
    @TargetAggregateIdentifier
    val memberId: Long,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val phoneNumber: String?,
    val street: String?,
    val city: String?,
    val zipCode: String?,
    val apartmentNo: String?,
    val floor: Int?,
    val birthDate: LocalDate?
) {
    constructor(
        memberId: Long,
        body: UpdateContactInformationRequest
    ) : this(memberId,
        body.firstName,
        lastName = body.lastName,
        email = body.email,
        street = body.address?.street,
        city = body.address?.city,
        zipCode = body.address?.zipCode,
        apartmentNo = body.address?.apartmentNo,
        floor = body.address?.floor,
        phoneNumber = body.phoneNumber,
        birthDate = body.birthDate
    )
}
