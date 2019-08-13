package com.hedvig.personservice.debts.web

import com.hedvig.personservice.debts.DebtService
import com.hedvig.personservice.debts.web.dtos.DebtDto
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
    fun check(@PathVariable ssn: String): ResponseEntity<Void> {
        debtService.checkPersonDebt(ssn)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{ssn}")
    fun debt(@PathVariable ssn: String): ResponseEntity<DebtDto> {
        val debtSnapshot = debtService.getPersonDebtSnapshot(ssn)
        return ResponseEntity.ok(DebtDto.from(debtSnapshot))
    }

    @GetMapping("/flag/{ssn}")
    fun flag(@PathVariable ssn: String) : ResponseEntity<Flag> {
        return ResponseEntity.ok(debtService.getDebtFlag(ssn))
    }
}
