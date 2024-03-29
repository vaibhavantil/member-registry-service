package com.hedvig.memberservice.identity

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.memberservice.events.MemberDeletedEvent
import com.hedvig.memberservice.events.MemberIdentifiedEvent
import com.hedvig.memberservice.identity.repository.IdentificationMethod
import com.hedvig.memberservice.identity.repository.IdentityEntity
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
    private val repository: IdentityRepository
) {

    @EventHandler
    fun on(event: MemberIdentifiedEvent) {

        val identityEntity = IdentityEntity(
            event.memberId,
            NationalIdentification(
                event.nationalIdentification.identification,
                when(event.nationalIdentification.nationality) {
                    MemberIdentifiedEvent.Nationality.SWEDEN -> Nationality.SWEDEN
                    MemberIdentifiedEvent.Nationality.NORWAY -> Nationality.NORWAY
                    MemberIdentifiedEvent.Nationality.DENMARK -> Nationality.DENMARK
                }
            ),
            when(event.identificationMethod) {
                MemberIdentifiedEvent.IdentificationMethod.NORWEGIAN_BANK_ID -> IdentificationMethod.NORWEGIAN_BANK_ID
                MemberIdentifiedEvent.IdentificationMethod.DANISH_BANK_ID -> IdentificationMethod.DANISH_BANK_ID
            },
            event.firstName,
            event.lastName
        )

        saveOrUpdate(identityEntity)
    }

    private fun saveOrUpdate(identityEntity: IdentityEntity) {
        repository.findByIdOrNull(identityEntity.memberId)?.let {
            if (it.hasNewOrMoreNewInfo(identityEntity)) {
                repository.save(it.update(identityEntity))
            }
        } ?: repository.save(identityEntity)
    }

    @EventHandler
    fun on(e: MemberDeletedEvent) {
        if (repository.findById(e.memberId).isPresent) {
            repository.deleteById(e.memberId)
        }
    }
}


