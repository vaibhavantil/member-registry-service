package com.hedvig.external.syna

import com.hedvig.external.syna.dto.SynaDebtCheckDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SynaServiceImpl @Autowired constructor(
    private val synaClient: SynaClient
): SynaService {
    override fun getDebtSnapshot(ssn: String): SynaDebtCheckDto {
        val synaQueryResponse = synaClient.getSynaResponse(ssn)
        return SynaDebtCheckDto.from(synaQueryResponse)
    }
}
