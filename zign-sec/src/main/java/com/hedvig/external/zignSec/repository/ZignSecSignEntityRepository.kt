package com.hedvig.external.zignSec.repository

import com.hedvig.external.zignSec.repository.entitys.ZignSecSignEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ZignSecSignEntityRepository: CrudRepository<ZignSecSignEntity, Long> {
    fun findByIdProviderPersonId(idProviderPersonId: String): Optional<ZignSecSignEntity>
}
