package com.hedvig.memberservice.identity

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.hedvig.memberservice.events.ZignSecSuccessfulAuthenticationEvent.AuthenticationMethod
import com.hedvig.memberservice.events.NorwegianMemberSignedEvent
import com.hedvig.memberservice.events.ZignSecSuccessfulAuthenticationEvent
import com.hedvig.memberservice.identity.repository.IdentificationMethod
import com.hedvig.memberservice.identity.repository.IdentityEntity
import com.hedvig.memberservice.identity.repository.IdentityEntity.Companion.hasNewOrMoreNewInfo
import com.hedvig.memberservice.identity.repository.IdentityRepository
import com.hedvig.memberservice.identity.repository.NationalIdentification
import com.hedvig.memberservice.identity.repository.Nationality
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("IdentifiedMembers")
class IdentityEventListener(
    private val repository: IdentityRepository,
    private val objectMapper: ObjectMapper
) {

    @EventHandler
    fun on(event: NorwegianMemberSignedEvent) {
        val identityEntity = IdentityEntity(
            event.memberId,
            NationalIdentification(
                event.ssn,
                Nationality.NORWAY
            ),
            IdentificationMethod.NORWEGIAN_BANK_ID,
            event.parseFirstNameFromZignSecJson(objectMapper),
            event.parseLastNameFromZignSecJson(objectMapper)
        )

        saveOrUpdate(identityEntity)
    }

    @EventHandler
    fun on(event: ZignSecSuccessfulAuthenticationEvent) {
        val (nationality, identificationMethod) = when (event.authenticationMethod) {
            AuthenticationMethod.NORWEGIAN_BANK_ID -> Pair(Nationality.NORWAY, IdentificationMethod.NORWEGIAN_BANK_ID)
            AuthenticationMethod.DANISH_BANK_ID -> Pair(Nationality.DENMARK, IdentificationMethod.DANISH_BANK_ID)
        }

        val identityEntity = IdentityEntity(
            event.memberId,
            NationalIdentification(
                event.ssn,
                nationality
            ),
            identificationMethod,
            event.parseFirstNameFromZignSecJson(objectMapper),
            event.parseLastNameFromZignSecJson(objectMapper)
        )

        saveOrUpdate(identityEntity)
    }

    private fun saveOrUpdate(identityEntity: IdentityEntity) {
        repository.findByIdOrNull(identityEntity.memberId)?.let {
            if (hasNewOrMoreNewInfo(identityEntity, it)) {
                repository.save(it.update(identityEntity))
            }
        } ?: repository.save(identityEntity)
    }
}


