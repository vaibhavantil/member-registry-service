package com.hedvig.external.zignSec.repository

import com.hedvig.external.zignSec.repository.entitys.ZignSecAuthenticationEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ZignSecAuthenticationEntityRepository: CrudRepository<ZignSecAuthenticationEntity, Long> {
    fun findByIdProviderPersonId(idProviderPersonId: String): ZignSecAuthenticationEntity?
    fun findByPersonalNumber(personalNumber: String): ZignSecAuthenticationEntity?
}
