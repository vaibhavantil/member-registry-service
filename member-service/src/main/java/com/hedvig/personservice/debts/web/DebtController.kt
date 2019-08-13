package com.hedvig.personservice.debts.web

import com.hedvig.personservice.debts.DebtService
import com.hedvig.personservice.persons.model.Flag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/_/debt")
class DebtController @Autowired constructor(
    private val debtService: DebtService
){
    @PostMapping("/check/{ssn}")
    fun checkDebt(@PathVariable ssn: String): ResponseEntity<Void> {
        debtService.checkPersonDebt(ssn)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/flag/{ssn}")
    fun getDebtFlag(@PathVariable ssn: String) : ResponseEntity<Flag> {
        val flag = debtService.getDebtFlagBySsn(ssn)
        return ResponseEntity.ok(flag)
    }
}
