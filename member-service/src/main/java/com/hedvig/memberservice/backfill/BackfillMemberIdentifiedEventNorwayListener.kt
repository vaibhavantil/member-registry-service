package com.hedvig.memberservice.backfill

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.hedvig.memberservice.events.MemberIdentifiedEvent
import com.hedvig.memberservice.events.NorwegianMemberSignedEvent
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.Timestamp
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * This is a one of and will be removed once its is used once
 */
@Component
@ProcessingGroup("BackfillMemberIdentifiedEventNorway")
class BackfillMemberIdentifiedEventNorwayListener(
    val objectMapper: ObjectMapper,
    val commandGateway: CommandGateway
) {

    val backfillUpUntilThisPoint = Instant.now()

    @EventListener
    fun on(event: NorwegianMemberSignedEvent, @Timestamp timestamp: Instant) {

        if (timestamp.isAfter(backfillUpUntilThisPoint)) {
            return
        }

        val memberIdentifiedCommand = MemberIdentifiedCommand(
            event.memberId,
            MemberIdentifiedEvent.NationalIdentification(event.ssn, MemberIdentifiedEvent.Nationality.NORWAY),
            MemberIdentifiedEvent.IdentificationMethod.NORWEGIAN_BANK_ID,
            parseFirstNameFromZignSecJson(event.providerJsonResponse),
            parseLastNameFromZignSecJson(event.providerJsonResponse)
        )
        commandGateway.send<Any>(memberIdentifiedCommand)
    }

    fun parseFirstNameFromZignSecJson(json: String): String? =
        objectMapper.readValue(json, ZignSecJson::class.java).identity.firstName

    fun parseLastNameFromZignSecJson(json: String): String? =
        objectMapper.readValue(json, ZignSecJson::class.java).identity.lastName

    data class ZignSecJson(
        val identity: ZignSecIdentityJson
    )

    @JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
    data class ZignSecIdentityJson(
        val firstName: String?,
        val lastName: String?
    )
}

data class MemberIdentifiedCommand(
    @AggregateIdentifier
    val id: Long,
    val nationalIdentification: MemberIdentifiedEvent.NationalIdentification,
    val identificationMethod: MemberIdentifiedEvent.IdentificationMethod,
    val firstName: String?,
    val lastName: String?
)
