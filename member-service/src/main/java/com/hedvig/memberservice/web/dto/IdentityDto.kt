package com.hedvig.memberservice.web.dto

import com.hedvig.memberservice.identity.repository.IdentityEntity
import com.hedvig.memberservice.identity.repository.Nationality

data class IdentityDto(
    val nationalIdentification: NationalIdentification,
    val firstName: String?,
    val lastName: String?
) {
    companion object {
        fun from(identityEntity: IdentityEntity) = IdentityDto(
            NationalIdentification(
                identityEntity.nationalIdentification.identification,
                when (identityEntity.nationalIdentification.nationality) {
                    Nationality.SWEDEN -> com.hedvig.memberservice.web.dto.Nationality.SWEDEN
                    Nationality.NORWAY -> com.hedvig.memberservice.web.dto.Nationality.NORWAY
                    Nationality.DENMARK -> com.hedvig.memberservice.web.dto.Nationality.DENMARK
                }
            ),
            identityEntity.firstName,
            identityEntity.lastName
        )
    }
}
