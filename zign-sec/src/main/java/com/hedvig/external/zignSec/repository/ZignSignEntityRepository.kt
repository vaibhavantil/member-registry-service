package com.hedvig.external.zignSec.repository

import com.hedvig.external.zignSec.repository.entitys.ZignSignEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ZignSignEntityRepository: CrudRepository<ZignSignEntity, Long> {
    fun findByIdProviderPersonId(idProviderPersonId: String): Optional<ZignSignEntity>
}
