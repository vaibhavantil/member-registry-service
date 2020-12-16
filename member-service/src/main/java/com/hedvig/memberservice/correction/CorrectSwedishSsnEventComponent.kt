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

    fun addCorrectionEventsOnAllSwedishMembers(): Int {
        val allMembers = memberRepository.findAll()
        var counter = 0

        allMembers.forEach { member ->
            if (correctMember.correctMember(member)) {
                counter++
            }
        }

        return counter
    }

}

