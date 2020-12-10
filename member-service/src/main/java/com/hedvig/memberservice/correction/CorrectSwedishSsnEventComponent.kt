package com.hedvig.memberservice.correction

import com.hedvig.memberservice.commands.UpdateBirthDateCommand
import com.hedvig.memberservice.events.BirthDateUpdatedEvent
import com.hedvig.memberservice.query.MemberRepository
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventsourcing.eventstore.EventStore
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.streams.toList

//This will be removed once used so I'm not going for the best code out there :D
//But it is really important that the logic is correct so please let me know if somthing it strange :)
@Component
class CorrectSwedishSsnEventComponent(
    private val eventStore: EventStore,
    private val memberRepository: MemberRepository,
    private val commandGateway: CommandGateway
) {

    fun addCorrectionEventsOnAllSwedishMembers(): Int {
        val allMembers = memberRepository.findAll()
        var counter = 0

        allMembers.forEach { member ->
            if (member.ssn != null && member.ssn.length != 12) {
                val birthDateUpdatedEvents = eventStore.readEvents(member.id.toString()).filter {
                    it.payloadType.simpleName == BirthDateUpdatedEvent::class.simpleName
                }.asStream().toList()

                if (birthDateUpdatedEvents.isEmpty()) {
                    getBirthDateFromSwedishSsn(member.ssn)?.let {
                        commandGateway.sendAndWait<Void>(UpdateBirthDateCommand(member.id, it))
                        counter++
                    }
                }
            }
        }

        return counter
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
