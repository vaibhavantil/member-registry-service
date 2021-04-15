package com.hedvig.auth.services

import com.hedvig.auth.models.*
import com.hedvig.auth.models.SwedishBankIdCredential
import com.hedvig.auth.models.SwedishBankIdCredentialRepository
import com.hedvig.auth.models.ZignSecCredential
import com.hedvig.auth.models.ZignSecCredentialRepository
import com.hedvig.memberservice.util.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserService {

    @Autowired private lateinit var auditEventRepository: AuditEventRepository

    @Autowired private lateinit var simpleSignConnectionRepository: SimpleSignConnectionRepository
    @Autowired private lateinit var swedishBankIdCredentialRepository: SwedishBankIdCredentialRepository
    @Autowired private lateinit var zignSecCredentialRepository: ZignSecCredentialRepository

    sealed class Credentials {
        data class SwedishBankID(
            val personalNumber: String,
            val simpleSignFallback: Boolean = true
        ): Credentials()

        data class ZignSec(
            val countryCode: String,
            val idProviderName: String,
            val idProviderPersonId: String,
            val personalNumber: String? = null,
            val simpleSignFallback: Boolean = true
        ): Credentials()
    }

    /**
     * Finds or creates a user matching the provided credentials.
     *
     * If a user with matching credentials is found it will be returned, but if no matching
     * credentials are found a new user is created with the credentials provided. The new user
     * becomes associated with the member that you specify in the `onboardingMemberId`
     * parameter. The next time you call `findOrCreateUserWithCredentials()` with the same
     * credentials, the previously created user is returned.
     *
     * If `onboardingMemberId` is `null`, then no user will be created.
     *
     * @param credentials The credentials used to lookup the user.
     * @param onboardingMemberId If a new user needs to be created it will be associated with this member.
     * @return A User matching the credentials, or null if the credentials are invalid.
     */
    fun findOrCreateUserWithCredentials(credentials: Credentials, onboardingMemberId: MemberId?): User? {
        return when(credentials) {
            is Credentials.SwedishBankID -> findOrCreateSwedishBankIdUser(credentials, onboardingMemberId)
            is Credentials.ZignSec -> findOrCreateZignSecUser(credentials, onboardingMemberId)
        }
    }

    private fun findOrCreateSwedishBankIdUser(credentials: Credentials.SwedishBankID, onboardingMemberId: MemberId?): User? {
        val swedishBankIdCredential = swedishBankIdCredentialRepository.findByPersonalNumber(
            credentials.personalNumber
        )
        return if (swedishBankIdCredential != null) {
            swedishBankIdCredential.user
        } else {
            if (credentials.simpleSignFallback) {
                val simpleSignConnection = simpleSignConnectionRepository.findByPersonalNumberAndCountry(
                    personalNumber = credentials.personalNumber,
                    country = "SE"
                )
                if (simpleSignConnection != null) {
                    return simpleSignConnection.user
                }
            }
            if (onboardingMemberId != null) {
                val user = User(associatedMemberId = onboardingMemberId)
                swedishBankIdCredentialRepository.saveAndFlush(
                    SwedishBankIdCredential(
                        personalNumber = credentials.personalNumber,
                        user = user
                    )
                )
                auditEventRepository.save(
                    AuditEvent(
                        user = user,
                        eventType = AuditEvent.EventType.CREATED_ON_LOGIN
                    )
                )
                return user
            }
            return null
        }
    }

    private fun findOrCreateZignSecUser(credentials: Credentials.ZignSec, onboardingMemberId: MemberId?): User? {
        val zignSecCredential = zignSecCredentialRepository.findByIdProviderNameAndIdProviderPersonId(
            credentials.idProviderName,
            credentials.idProviderPersonId
        )
        return if (zignSecCredential != null) {
            zignSecCredential.user
        } else {
            if (credentials.simpleSignFallback && credentials.personalNumber != null) {
                val simpleSignConnection = simpleSignConnectionRepository.findByPersonalNumberAndCountry(
                    personalNumber = credentials.personalNumber,
                    country = credentials.countryCode
                )
                if (simpleSignConnection != null) {
                    return simpleSignConnection.user
                }
            }
            if (onboardingMemberId != null) {
                val user = User(associatedMemberId = onboardingMemberId)
                zignSecCredentialRepository.saveAndFlush(
                    ZignSecCredential(
                        idProviderName = credentials.idProviderName,
                        idProviderPersonId = credentials.idProviderPersonId,
                        user = user
                    )
                )
                auditEventRepository.save(
                    AuditEvent(
                        user = user,
                        eventType = AuditEvent.EventType.CREATED_ON_LOGIN
                    )
                )
                return user
            }
            return null
        }
    }

}
