package com.hedvig.external.syna

import com.hedvig.external.syna.dto.SynaDebtCheckDto

interface SynaService {
    fun getDebtCheck(ssn: String): SynaDebtCheckDto
}
