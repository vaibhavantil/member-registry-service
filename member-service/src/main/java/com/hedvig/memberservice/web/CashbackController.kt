package com.hedvig.memberservice.web

import com.hedvig.memberservice.aggregates.PickedLocale
import com.hedvig.memberservice.commands.SelectNewCashbackCommand
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.services.CashbackService
import com.hedvig.memberservice.web.dto.CashbackOption
import com.hedvig.memberservice.web.dto.PickedLocaleDTO
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import java.util.stream.Collectors

@RestController
@RequestMapping("/cashback")
class CashbackController @Autowired constructor(
    var commandGateway: CommandGateway,
    val memberRepository: MemberRepository,
    private val cashbackService: CashbackService
) {
    @GetMapping("options")
    fun options(
        @RequestHeader(value = "hedvig.token") hid: Long
    ): ResponseEntity<List<CashbackOption>> {
        val member = memberRepository.findById(hid)
        if (member.isPresent) {
            val cashbackOptions: List<CashbackOption> = cashbackService
                .getOptions(member.get().pickedLocale)
                .map { o: CashbackOption ->
                    if (o.id.toString() == member.get().getCashbackId()) {
                        return@map o.copy(selected = true)
                    }
                    o
                }
            return ResponseEntity.ok(cashbackOptions)
        }
        return ResponseEntity.notFound().build()
    }

    @PostMapping("")
    fun cashback(
        @RequestHeader(value = "hedvig.token") hid: Long, @RequestParam optionId: UUID?
    ): ResponseEntity<String> {
        val member = memberRepository.findById(hid)
        if (member.isEmpty) {
            return ResponseEntity.notFound().build()
        }
        val opt = cashbackService.getCashbackOption(optionId, member.get().pickedLocale ?: DEFAULT_LOCALE)
        return if (opt.isPresent && member.isPresent) {
            commandGateway.sendAndWait<Any>(SelectNewCashbackCommand(hid, optionId))
            ResponseEntity.noContent().build() // ok().build();
        } else {
            ResponseEntity.notFound().build()
        }
    }

    private val DEFAULT_LOCALE = PickedLocale.sv_SE
}
