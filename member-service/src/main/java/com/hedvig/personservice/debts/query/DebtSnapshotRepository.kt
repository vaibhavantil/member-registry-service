package com.hedvig.personservice.debts.query

import com.hedvig.personservice.debts.model.DebtSnapshot
import org.springframework.data.repository.CrudRepository
import java.util.*

interface DebtSnapshotRepository: CrudRepository<DebtSnapshot, UUID>
