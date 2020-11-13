package com.hedvig.memberservice.web

import com.hedvig.memberservice.aggregates.PickedLocale
import com.hedvig.memberservice.aggregates.toPickedLocale
import com.hedvig.memberservice.services.cashback.CashbackService
import com.hedvig.memberservice.services.exceptions.MemberNotFoundException
import com.hedvig.memberservice.web.dto.CashbackOption
import com.hedvig.resolver.LocaleResolver
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
        @RequestHeader(value = "hedvig.token") memberId: Long,
        @RequestHeader(value = "Locale", required = false) locale: String?,
        @RequestHeader(value = "Accept-Language", required = false) acceptLanguage: String?
    ): ResponseEntity<List<CashbackOption>> {
        return try {
            val cashbackOptions: List<CashbackOption> = cashbackService
                .getOptions(memberId, resolveLocaleFromHeaders(locale, acceptLanguage))
            ResponseEntity.ok(cashbackOptions)
        } catch (e: MemberNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("")
    fun cashback(
        @RequestHeader(value = "hedvig.token") hid: Long, @RequestParam optionId: UUID?,
        @RequestHeader(value = "Locale", required = false) locale: String?,
        @RequestHeader(value = "Accept-Language", required = false) acceptLanguage: String?
    ): ResponseEntity<String> {
        return try {
            optionId?.let {
                cashbackService.selectCashbackOption(hid, it, resolveLocaleFromHeaders(locale, acceptLanguage))
                return ResponseEntity.noContent().build()
            } ?: ResponseEntity.notFound().build()
        } catch (e: MemberNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    private fun resolveLocaleFromHeaders(locale: String?, acceptLanguage: String?): PickedLocale? {
        locale?.let {
            try {
                return PickedLocale.valueOf(it)
            } catch (e: Exception) {
                // no-op
            }
        }

        return LocaleResolver
            .resolveNullableLocale(acceptLanguage)
            ?.toPickedLocale()
    }
}
