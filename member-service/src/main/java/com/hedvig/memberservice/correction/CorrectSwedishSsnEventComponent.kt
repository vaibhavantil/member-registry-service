package com.hedvig.memberservice.correction

import com.hedvig.memberservice.query.MemberRepository
import org.springframework.stereotype.Component

//This will be removed once used so I'm not going for the best code out there :D
//But it is really important that the logic is correct so please let me know if somthing it strange :)
@Component
class CorrectSwedishSsnEventComponent(
    private val memberRepository: MemberRepository,
    private val correctMember: CorrectMember
) {

    fun addCorrectionEventsOnAllSwedishMembers(): String {
        val allMembers = memberRepository.findAll()
        var counter = 0
        var oomeMembersMemberIds = mutableListOf<Long>()

        allMembers.forEach { member ->
            try {
                if (correctMember.correctMember(member)) {
                    counter++
                }
            } catch (e: OutOfMemoryError) {
                oomeMembersMemberIds.add(member.id)
            }
        }

        return "counter: $counter oomeCounter: ${oomeMembersMemberIds.size} [$oomeMembersMemberIds]"
    }

}

