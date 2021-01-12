package com.hedvig.memberservice.identity

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.hedvig.memberservice.events.ZignSecSuccessfulAuthenticationEvent.AuthenticationMethod
import com.hedvig.memberservice.events.NorwegianMemberSignedEvent
import com.hedvig.memberservice.events.ZignSecSuccessfulAuthenticationEvent
import com.hedvig.memberservice.identity.repository.IdentificationMethod
import com.hedvig.memberservice.identity.repository.IdentityEntity
import com.hedvig.memberservice.identity.repository.IdentityRepository
import com.hedvig.memberservice.identity.repository.NationalIdentification
import com.hedvig.memberservice.identity.repository.Nationality
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("IdentityEventListener")
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
            paresFullNameFromZignSecJson(event.providerJsonResponse)
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
            paresFullNameFromZignSecJson(event.providerJsonResponse)
        )

        saveOrUpdate(identityEntity)
    }

    private fun saveOrUpdate(identityEntity: IdentityEntity) {
        repository.findById(identityEntity.memberId).ifPresentOrElse(
            {
                if (identityEntity.hasNewOrMoreNewInfo(it)) {
                    repository.save(
                        it.update(identityEntity)
                    )
                }
            },
            {
                repository.save(identityEntity)
            }
        )
    }

    private fun paresFullNameFromZignSecJson(json: String): String? =
        objectMapper.readValue(json, ZignSecJson::class.java).identity.fullName

    companion object {
        fun IdentityEntity.hasNewOrMoreNewInfo(oldIdentityEntity: IdentityEntity): Boolean {
            if (this.memberId != oldIdentityEntity.memberId) {
                throw IllegalCallerException("hasNewOrMoreNewInfo should not be called with entities that has different member id")
            }

            if (
                this.nationalIdentification == oldIdentityEntity.nationalIdentification ||
                this.identificationMethod == oldIdentityEntity.identificationMethod ||
                this.fullName == oldIdentityEntity.fullName
            ) {
                return false
            }

            if (this.fullName == null) {
                return false
            }

            return true
        }
    }
}

data class ZignSecJson(
    val identity: ZignSecJsonIdentity
)

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
data class ZignSecJsonIdentity(
    val firstName: String?,
    val lastName: String?,
    val fullName: String?
)

