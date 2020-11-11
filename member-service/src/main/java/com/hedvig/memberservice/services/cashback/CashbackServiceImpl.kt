package com.hedvig.memberservice.services.cashback

import com.hedvig.integration.contentservice.ContentServiceClient
import com.hedvig.memberservice.aggregates.PickedLocale
import com.hedvig.memberservice.aggregates.toPickedLocale
import com.hedvig.memberservice.query.MemberEntity
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.services.exceptions.MemberNotFoundException
import com.hedvig.memberservice.web.dto.CashbackOption
import com.hedvig.resolver.LocaleResolver
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.*

@Component
@ConditionalOnProperty(name = ["localizedCashback"], havingValue = "true")
class CashbackServiceImpl(
    private val contentServiceClient: ContentServiceClient,
    private val memberRepository: MemberRepository
) : CashbackService {
    override fun getCashbackOption(memberId: Long): Optional<CashbackOption> {
        val member = findMemberOrThrow(memberId)

        if (member.cashbackId == null)
            return Optional.empty()

        val locale = member.resolvePickedLocaleOrThrow()

        val cashbackOption = contentServiceClient.cashbackOption(member.cashbackId, locale.name).body

        return cashbackOption?.let { Optional.of(it.toCashbackOption(true)) } ?: Optional.empty()
    }

    override fun getOptions(memberId: Long): List<CashbackOption> {
        val member = findMemberOrThrow(memberId)

        val cashbackOptions = contentServiceClient.cashbackOptions(member.resolvePickedLocaleOrThrow().name).body!!

        return cashbackOptions.map { it.toCashbackOption(member.cashbackId == it.id.toString()) }
    }

    override fun getDefaultId(memberId: Long): UUID = when (findMemberOrThrow(memberId).resolvePickedLocaleOrThrow()) {
        PickedLocale.sv_SE, PickedLocale.en_SE -> DEFAULT_SWEDISH_CASHBACK_OPTION
        PickedLocale.nb_NO, PickedLocale.en_NO -> DEFAULT_NORWEGIAN_CASHBACK_OPTION
        PickedLocale.da_DK, PickedLocale.en_DK -> DEFAULT_DANISH_CASHBACK_OPTION
    }

    override fun getDefaultCashback(memberId: Long): CashbackOption? {
        val member = findMemberOrThrow(memberId)

        val cashbackOption = contentServiceClient.cashbackOption(getDefaultId(memberId).toString(), member.resolvePickedLocaleOrThrow().name).body

        return cashbackOption?.let { it.toCashbackOption(true) }
    }

    private fun MemberEntity.resolvePickedLocaleOrThrow() = this.pickedLocale
        ?: LocaleResolver.resolveNullableLocale(this.acceptLanguage)?.toPickedLocale()
        ?: throw RuntimeException("Could not resolve locale for member: ${this.id}")

    private fun findMemberOrThrow(memberId: Long) = memberRepository.findById(memberId).orElseGet {
        throw MemberNotFoundException(memberId)
    }

    companion object {
        private val DEFAULT_SWEDISH_CASHBACK_OPTION: UUID = UUID.fromString("97b2d1d8-af4a-11e7-9b2b-bbc138162bb2")
        private val DEFAULT_NORWEGIAN_CASHBACK_OPTION: UUID = UUID.fromString("02c99ad8-75aa-11ea-bc55-0242ac130003")
        private val DEFAULT_DANISH_CASHBACK_OPTION: UUID = UUID.fromString("a04ea632-70a3-4ec9-bc19-84d5069947e3")
    }
}
