package com.hedvig.memberservice.correction

import com.hedvig.memberservice.commands.UpdateBirthDateCommand
import com.hedvig.memberservice.events.BirthDateUpdatedEvent
import com.hedvig.memberservice.query.MemberEntity
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventsourcing.eventstore.EventStore
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.transaction.Transactional

class CorrectMember(
    private val eventStore: EventStore,
    private val commandGateway: CommandGateway
) {

    fun correctMember(member: MemberEntity): Boolean {
        if (member.ssn != null && member.ssn.length == 12) {
            val birthDateUpdatedEvents = eventStore.readEvents(member.id.toString()).filter {
                it.payloadType.simpleName == BirthDateUpdatedEvent::class.simpleName
            }.hasNext()

            if (!birthDateUpdatedEvents) {
                getBirthDateFromSwedishSsn(member.ssn)?.let {
                    commandGateway.sendAndWait<Void>(UpdateBirthDateCommand(member.id, it))
                    return true
                }
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
