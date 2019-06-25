package com.hedvig.memberservice.web

import com.hedvig.external.syna.SynaService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/_/debt")
class DebtController @Autowired constructor(
        private val synaService: SynaService
){
    @GetMapping("{ssn}")
    fun get(@PathVariable ssn: String): Any {
        return synaService.getDebtCheck(ssn)
    }
}
