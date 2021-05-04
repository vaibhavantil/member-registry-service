package com.hedvig.auth.services

import com.hedvig.auth.models.*
import com.hedvig.auth.models.SwedishBankIdCredential
import com.hedvig.auth.models.SwedishBankIdCredentialRepository
import com.hedvig.auth.models.ZignSecCredential
import com.hedvig.auth.models.ZignSecCredentialRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * The `UserService` can be used to lookup (and create or delete) a users. A user is associated
 * with a `MemberId` and a unique set of credentials. The credentials can be used to lookup a user.
 * To find a user with a matching credential you use the `findOrCreateUserWithCredential()`
 * method.
 *
 * @see User
 *
 * @author Niklas Holmgren <niklas@hedvig.com>
 * @author Fredrik Tõnisson-Bystam <fredrik.bystam@hedvig.com>
 */
@Service
class UserService {

    @Autowired private lateinit var auditEventRepository: AuditEventRepository
    @Autowired private lateinit var userRepository: UserRepository

    @Autowired private lateinit var simpleSignConnectionRepository: SimpleSignConnectionRepository
    @Autowired private lateinit var swedishBankIdCredentialRepository: SwedishBankIdCredentialRepository
    @Autowired private lateinit var zignSecCredentialRepository: ZignSecCredentialRepository

    @Value("\${hedvig.auth.canCreateUsersOnLogin:false}")
    private var canCreateUsersOnLogin: Boolean = false

    /**
     * The following credential types are currently supported:
     *
     * - SwedishBankId
     * - ZignSec
     * - SimpleSign
     */
    sealed class Credential {
        /**
         * == Swedish BankID ==
         * An ordinary Swedish personal number is used as credential to uniquely identify a user.
         */
        data class SwedishBankID(
            val personalNumber: String,
            val simpleSignFallback: SimpleSign? = null
        ) : Credential()

        /**
         * == ZignSec ==
         * Uses the `idProviderName` (the name of the underlying identity provider) and `idProviderPersonId`
         * (a unique identifier for a person) as credential to uniquely identify a user. Can also include
         * an optional personal number if SimpleSign fallback is needed (see below).
         */
        data class ZignSec(
            val idProviderName: String,
            val idProviderPersonId: String,
            val simpleSignFallback: SimpleSign? = null
        ) : Credential()

        /**
         * == SimpleSign ==
         *  SimpleSign is a method for signing contracts without authentication. When calling
         * `findOrCreateUserWithCredential()` and no user with matching credential is found,
         * **SimpleSign** can be used as a fallback method. A user will be looked up based
         * on the personal number provided when signing with SimpleSign.
         *
         * Note that SimpleSign is not a credential in a strict sense, because the personal
         * number is completely unauthenticated, so can not be used on its own as a credential
         * type, only as a fallback for another authenticated credential type.
         *
         * SimpleSign fallback can be turned on by setting the `simpleSignFallback` property
         * on a credential type that supports it. SimpleSign fallback is turned off by default.
         */
        data class SimpleSign(
            val countryCode: String,
            val personalNumber: String
        ): Credential()
    }

    /**
     * Describes the context in which the call to `findOrCreateUserWithCredential()` is being made.
     *
     * @param onboardingMemberId If a new user needs to be created it will be associated with this member.
     * @param authType The type of authentication that was used to obtain the credentials (defaults to LOGIN).
    */
    data class Context(
        val onboardingMemberId: MemberId?,
        val authType: AuthType = AuthType.LOGIN
    ) {}

    /**
     * Type of authentication (used for auditing).
     */
    enum class AuthType {
        LOGIN,
        SIGN
    }

    /**
     * Finds or creates a user matching the provided credential.
     *
     * If a user with matching credentials is found it will be returned, but if no matching
     * credential are found a new user is created with the credential provided. The new user
     * becomes associated with the member that you specify in the `onboardingMemberId` property
     * of the `context` parameter. The next time you call `findOrCreateUserWithCredential()`
     * with the same credential, the previously created user is returned.
     *
     * If `onboardingMemberId` is `null`, then no user will be created.
     *
     * @see Credential
     * @see Context
     * @see User
     *
     * @param credential The credential used to lookup the user.
     * @param context The context in which the lookup is made.
     * @return A User matching the credential, or null if the credential is invalid.
     */
    fun findOrCreateUserWithCredential(credential: Credential, context: Context): User? {
        return when (credential) {
            is Credential.SwedishBankID -> findOrCreateSwedishBankIdUser(credential, context)
            is Credential.ZignSec -> findOrCreateZignSecUser(credential, context)
            is Credential.SimpleSign -> findOrCreateSimpleSignUser(credential, context)
        }
    }

    private fun findOrCreateSwedishBankIdUser(
        credential: Credential.SwedishBankID,
        context: Context
    ): User? {
        val swedishBankIdCredential = swedishBankIdCredentialRepository.findByPersonalNumber(
            credential.personalNumber
        )
        return if (swedishBankIdCredential != null) {
            swedishBankIdCredential.user
        } else {
            if (credential.simpleSignFallback != null) {
                val simpleSignConnection = simpleSignConnectionRepository.findByPersonalNumberAndCountry(
                    personalNumber = credential.simpleSignFallback.personalNumber,
                    country = credential.simpleSignFallback.countryCode
                )
                if (simpleSignConnection != null) {
                    return simpleSignConnection.user
                }
            }
            if (context.onboardingMemberId != null && canCreateUsersOnLogin) {
                val user = User(associatedMemberId = context.onboardingMemberId)
                swedishBankIdCredentialRepository.saveAndFlush(
                    SwedishBankIdCredential(
                        personalNumber = credential.personalNumber,
                        user = user
                    )
                )
                auditEventRepository.saveAndFlush(
                    AuditEvent(
                        user = user,
                        eventType = when (context.authType) {
                            AuthType.LOGIN -> AuditEvent.EventType.CREATED_ON_LOGIN
                            AuthType.SIGN -> AuditEvent.EventType.CREATED_ON_IMPORT
                        }
                    )
                )
                return user
            }
            return null
        }
    }

    private fun findOrCreateZignSecUser(credential: Credential.ZignSec, context: Context): User? {
        val zignSecCredential = zignSecCredentialRepository.findByIdProviderNameAndIdProviderPersonId(
            credential.idProviderName,
            credential.idProviderPersonId
        )
        return if (zignSecCredential != null) {
            zignSecCredential.user
        } else {
            if (credential.simpleSignFallback != null) {
                val simpleSignConnection = simpleSignConnectionRepository.findByPersonalNumberAndCountry(
                    personalNumber = credential.simpleSignFallback.personalNumber,
                    country = credential.simpleSignFallback.countryCode
                )
                if (simpleSignConnection != null) {
                    return simpleSignConnection.user
                }
            }
            if (context.onboardingMemberId != null && canCreateUsersOnLogin) {
                val user = User(associatedMemberId = context.onboardingMemberId)
                zignSecCredentialRepository.saveAndFlush(
                    ZignSecCredential(
                        idProviderName = credential.idProviderName,
                        idProviderPersonId = credential.idProviderPersonId,
                        user = user
                    )
                )
                auditEventRepository.saveAndFlush(
                    AuditEvent(
                        user = user,
                        eventType = when (context.authType) {
                            AuthType.LOGIN -> AuditEvent.EventType.CREATED_ON_LOGIN
                            AuthType.SIGN -> AuditEvent.EventType.CREATED_ON_IMPORT
                        }
                    )
                )
                return user
            }
            return null
        }
    }

    private fun findOrCreateSimpleSignUser(credential: Credential.SimpleSign, context: Context): User? {
        val simpleSignConnection = simpleSignConnectionRepository.findByPersonalNumberAndCountry(
            credential.personalNumber,
            credential.countryCode
        )
        return if (simpleSignConnection != null) {
            simpleSignConnection.user
        } else {
            if (context.onboardingMemberId != null && canCreateUsersOnLogin) {
                val user = User(associatedMemberId = context.onboardingMemberId)
                simpleSignConnectionRepository.saveAndFlush(
                    SimpleSignConnection(
                        country = credential.countryCode,
                        personalNumber = credential.personalNumber,
                        user = user
                    )
                )
                auditEventRepository.saveAndFlush(
                    AuditEvent(
                        user = user,
                        eventType = when (context.authType) {
                            AuthType.LOGIN -> AuditEvent.EventType.CREATED_ON_LOGIN
                            AuthType.SIGN -> AuditEvent.EventType.CREATED_ON_IMPORT
                        }
                    )
                )
                return user
            }
            return null
        }
    }

    /**
     * Find a user that is associated with a specific member ID.
     *
     * @param memberId The associated member ID.
     * @return A User object or null if no user is found.
     */
    fun findUserByAssociatedMemberId(memberId: MemberId): User? {
        return userRepository.findByAssociatedMemberId(memberId)
    }

    /**
     * Delete a user and all related data.
     *
     * @param memberId The associated member ID of the user to delete.
     */
    fun deleteUserWithAssociatedMemberId(memberId: MemberId) {
        userRepository.flush()
        findUserByAssociatedMemberId(memberId)?.let { user ->
            userRepository.delete(user)
            userRepository.flush()
        }
    }

}
