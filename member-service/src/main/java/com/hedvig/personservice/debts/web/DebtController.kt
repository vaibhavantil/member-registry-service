package com.hedvig.personservice.debts.web

import com.hedvig.personservice.debts.DebtService
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

    @PostMapping("/check/all/signed")
    fun checkAllDebt(): ResponseEntity<Void> {
        debtService.checkAllPersonDebts()
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/check/nonchecked/signed")
    fun checkNonCheckedDebt(): ResponseEntity<Void> {
        debtService.checkNonCheckedPersonDebts()
        return ResponseEntity.noContent().build()
    }
}
