package com.hedvig.memberservice.web

import com.hedvig.memberservice.services.cashback.CashbackService
import com.hedvig.memberservice.services.exceptions.MemberNotFoundException
import com.hedvig.memberservice.web.dto.CashbackOption
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/cashback")
class CashbackController @Autowired constructor(
    private val cashbackService: CashbackService
) {
    @GetMapping("options")
    fun options(
        @RequestHeader(value = "hedvig.token") memberId: Long
    ): ResponseEntity<List<CashbackOption>> {
        return try {
            val cashbackOptions: List<CashbackOption> = cashbackService
                .getOptions(memberId)
            ResponseEntity.ok(cashbackOptions)
        } catch (e: MemberNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("")
    fun cashback(
        @RequestHeader(value = "hedvig.token") hid: Long, @RequestParam optionId: UUID?
    ): ResponseEntity<String> {
        return try {
            optionId?.let {
                cashbackService.selectCashbackOption(hid, it)
                return ResponseEntity.noContent().build()
            } ?: ResponseEntity.notFound().build()
        } catch (e: MemberNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }
}
