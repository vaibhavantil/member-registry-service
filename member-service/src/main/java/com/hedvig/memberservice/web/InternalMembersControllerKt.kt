package com.hedvig.memberservice.web

import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.web.dto.ChargeMembersDTO
import com.hedvig.memberservice.web.dto.InternalMember
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/i/member", "/_/member"])
class InternalMembersControllerKt(
    val memberRepository: MemberRepository
) {

    @PostMapping("/many")
    fun getMembers(@RequestBody dto: ChargeMembersDTO): ResponseEntity<List<InternalMember>> {
        val memberIds = dto.memberIds.map { it.toLong() }
        val membersReducedList = memberRepository.findAllByIdIn(memberIds)
        val members = memberIds.map { memberId -> InternalMember.fromEntity(membersReducedList.first { it.id == memberId }) }

        if (dto.memberIds.size != members.size) {
            log.error(
                "Length mismatch of supplied members and found members: wanted {}, found {}",
                dto.memberIds.size,
                members.size)
            return ResponseEntity.notFound().build()
        }
        return ResponseEntity.ok(members)
    }

    private val log = LoggerFactory.getLogger(this::class.java)
}
