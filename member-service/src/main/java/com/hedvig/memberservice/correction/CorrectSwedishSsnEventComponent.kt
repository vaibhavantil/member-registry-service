package com.hedvig.memberservice.correction

import com.hedvig.memberservice.query.MemberRepository
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventsourcing.eventstore.EventStore
import org.springframework.stereotype.Component

//This will be removed once used so I'm not going for the best code out there :D
//But it is really important that the logic is correct so please let me know if somthing it strange :)
@Component
class CorrectSwedishSsnEventComponent(
    private val memberRepository: MemberRepository,
    private val eventStore: EventStore,
    private val commandGateway: CommandGateway
) {

    fun addCorrectionEventsOnAllSwedishMembers(): String {
        val allMembers = memberRepository.findAll()
        var counter = 0

        allMembers.forEach { member ->
            var correctMemberJob: CorrectMember? = CorrectMember(eventStore, commandGateway)
            if (correctMemberJob?.correctMember(member) == true) {
                counter++
            }
            correctMemberJob = null
            System.gc()
        }

        return "counter: $counter"
    }

}

