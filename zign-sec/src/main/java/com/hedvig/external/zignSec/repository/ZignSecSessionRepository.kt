package com.hedvig.external.zignSec.repository

import com.hedvig.external.zignSec.repository.entitys.ZignSecSession
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ZignSecSessionRepository: CrudRepository<ZignSecSession, UUID>
