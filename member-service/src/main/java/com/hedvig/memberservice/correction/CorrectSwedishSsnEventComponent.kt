package com.hedvig.memberservice.correction

import com.hedvig.memberservice.commands.UpdateBirthDateCommand
import com.hedvig.memberservice.events.BirthDateUpdatedEvent
import com.hedvig.memberservice.query.MemberEntity
import com.hedvig.memberservice.query.MemberRepository
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventsourcing.eventstore.EventStore
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

//This will be removed once used so I'm not going for the best code out there :D
//But it is really important that the logic is correct so please let me know if somthing it strange :)
@Component
@Transactional
class CorrectSwedishSsnEventComponent(
    private val memberRepository: MemberRepository,
    private val eventStore: EventStore,
    private val commandGateway: CommandGateway
) {

    fun addCorrectionEventsOnAllSwedishMembers(): String {
        val allMembers = memberRepository.findAllSwedishMembers()
        var counter = 0

        allMembers.forEach { member ->
            if(correctMember(member)){
                counter++
            }
        }

        return "counter: $counter"
    }

    fun correctMember(member: MemberEntity): Boolean {
        val birthDateUpdatedEvents = eventStore.readEvents(member.id.toString()).filter {
            it.payloadType.simpleName == BirthDateUpdatedEvent::class.simpleName
        }.hasNext()

        if (!birthDateUpdatedEvents) {
            getBirthDateFromSwedishSsn(member.ssn)?.let {
                commandGateway.send<Void>(UpdateBirthDateCommand(member.id, it))
                return true
            }
        }

        return false
    }

    private fun getBirthDateFromSwedishSsn(ssn: String): LocalDate? {
        return try {
            val dtf = DateTimeFormatter.ofPattern("yyyyMMdd")
            LocalDate.parse(ssn.substring(0, 8), dtf)
        } catch (exception: Exception) {
            null
        }
    }
}

