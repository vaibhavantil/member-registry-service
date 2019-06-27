package com.hedvig.personservice.debts.web

import com.hedvig.personservice.debts.DebtService
import com.hedvig.personservice.debts.model.DebtSnapshot
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/_/debt")
class DebtController @Autowired constructor(
        private val debtService: DebtService
){
    @GetMapping("/{ssn}")
    fun check(@PathVariable ssn: String): ResponseEntity<DebtSnapshot> {
        return ResponseEntity.ok(debtService.getPersonDebtSnapshot(ssn))
    }
}
