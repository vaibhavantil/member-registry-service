package com.hedvig.memberservice.aggregates

import org.axonframework.commandhandling.model.AggregateLifecycle.apply

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.common.UUIDGenerator
import com.hedvig.external.bisnodeBCI.BisnodeClient
import com.hedvig.memberservice.backfill.MemberIdentifiedCommand
import com.hedvig.memberservice.commands.CreateMemberCommand
import com.hedvig.memberservice.commands.EditMemberInfoCommand
import com.hedvig.memberservice.commands.EditMemberInformationCommand
import com.hedvig.memberservice.commands.InactivateMemberCommand
import com.hedvig.memberservice.commands.InitializeAppleUserCommand
import com.hedvig.memberservice.commands.MemberSimpleSignedCommand
import com.hedvig.memberservice.commands.MemberUpdateContactInformationCommand
import com.hedvig.memberservice.commands.SelectNewCashbackCommand
import com.hedvig.memberservice.commands.SetFraudulentStatusCommand
import com.hedvig.memberservice.commands.SignMemberFromUnderwriterCommand
import com.hedvig.memberservice.commands.StartSwedishOnboardingWithSSNCommand
import com.hedvig.memberservice.commands.SwedishBankIdAuthenticationAttemptCommand
import com.hedvig.memberservice.commands.SwedishBankIdSignCommand
import com.hedvig.memberservice.commands.UpdateAcceptLanguageCommand
import com.hedvig.memberservice.commands.UpdateBirthDateCommand
import com.hedvig.memberservice.commands.UpdateEmailCommand
import com.hedvig.memberservice.commands.UpdatePhoneNumberCommand
import com.hedvig.memberservice.commands.UpdatePickedLocaleCommand
import com.hedvig.memberservice.commands.UpdateSSNCommand
import com.hedvig.memberservice.commands.UpdateSwedishWebOnBoardingInfoCommand
import com.hedvig.memberservice.commands.ZignSecSignCommand
import com.hedvig.memberservice.commands.ZignSecSuccessfulAuthenticationCommand
import com.hedvig.memberservice.commands.models.ZignSecAuthenticationMarket
import com.hedvig.memberservice.events.AcceptLanguageUpdatedEvent
import com.hedvig.memberservice.events.BirthDateUpdatedEvent
import com.hedvig.memberservice.events.DanishMemberSignedEvent
import com.hedvig.memberservice.events.DanishSSNUpdatedEvent
import com.hedvig.memberservice.events.EmailUpdatedEvent
import com.hedvig.memberservice.events.FraudulentStatusUpdatedEvent
import com.hedvig.memberservice.events.InsuranceCancellationEvent
import com.hedvig.memberservice.events.LivingAddressUpdatedEvent
import com.hedvig.memberservice.events.MemberAuthenticatedEvent
import com.hedvig.memberservice.events.MemberCancellationEvent
import com.hedvig.memberservice.events.MemberCreatedEvent
import com.hedvig.memberservice.events.MemberInactivatedEvent
import com.hedvig.memberservice.events.MemberSignedEvent
import com.hedvig.memberservice.events.MemberSignedWithoutBankId
import com.hedvig.memberservice.events.MemberSimpleSignedEvent
import com.hedvig.memberservice.events.MemberStartedOnBoardingEvent
import com.hedvig.memberservice.events.NameUpdatedEvent
import com.hedvig.memberservice.events.NewCashbackSelectedEvent
import com.hedvig.memberservice.events.NorwegianMemberSignedEvent
import com.hedvig.memberservice.events.NorwegianSSNUpdatedEvent
import com.hedvig.memberservice.events.OnboardingStartedWithSSNEvent
import com.hedvig.memberservice.events.PersonInformationFromBisnodeEvent
import com.hedvig.memberservice.events.PhoneNumberUpdatedEvent
import com.hedvig.memberservice.events.PickedLocaleUpdatedEvent
import com.hedvig.memberservice.events.SSNUpdatedEvent
import com.hedvig.memberservice.events.TrackingIdCreatedEvent
import com.hedvig.memberservice.events.MemberIdentifiedEvent
import com.hedvig.memberservice.services.cashback.CashbackService
import com.hedvig.memberservice.util.SsnUtilImpl.Companion.getBirthdateFromSwedishSsn
import com.hedvig.memberservice.util.logger
import com.hedvig.memberservice.web.dto.Nationality.Companion.toMemberSimpleSignedEventNationality
import com.hedvig.memberservice.web.dto.Nationality.Companion.toSSNUpdatedEventNationality
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.commandhandling.model.ApplyMore
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.messaging.MetaData
import org.axonframework.spring.stereotype.Aggregate
import org.springframework.beans.factory.annotation.Autowired
import java.io.IOException
import java.util.UUID
import java.util.function.Supplier

@Aggregate
class MemberAggregate() {
    @AggregateIdentifier
    var _id: Long? = null
    val id: Long
        get() = _id!!

    @Autowired
    lateinit var bisnodeClient: BisnodeClient

    @Autowired
    lateinit var cashbackService: CashbackService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var uuidGenerator: UUIDGenerator

    lateinit var status: MemberStatus
    lateinit var member: Member
    var trackingId: UUID? = null
    var latestBisnodeInformation: BisnodeInformation? = null

    @CommandHandler
    constructor(command: CreateMemberCommand) : this() {
        apply(MemberCreatedEvent(command.memberId, MemberStatus.INITIATED))
            .andThenApply {
                if (command.acceptLanguage != null && !command.acceptLanguage.isEmpty()) {
                    AcceptLanguageUpdatedEvent(command.memberId, command.acceptLanguage)
                } else null
            }
    }

    @CommandHandler
    fun handle(command: SwedishBankIdAuthenticationAttemptCommand) {
        var applyChain: ApplyMore? = null
        val bankIdAuthResponse = command.bankIdAuthResponse
        if (status == MemberStatus.INITIATED || status == MemberStatus.ONBOARDING) {
            // Trigger fetching of bisnode data.
            val ssn = bankIdAuthResponse.ssn
            applyChain = apply(SSNUpdatedEvent(id, ssn, SSNUpdatedEvent.Nationality.SWEDEN))
            // -- Tracking id generation for the new member id
            generateTrackingId()
            applyChain = getBirthdateFromSwedishSsnAndApplyEvent(ssn, applyChain)
            applyChain = try {
                getPersonInformationFromBisnode(applyChain!!, ssn)
            } catch (ex: RuntimeException) {
                logger.error("Caught exception calling bisnode for personalInformation", ex)
                applyChain!!.andThenApply {
                    NameUpdatedEvent(
                        id,
                        formatName(bankIdAuthResponse.givenName),
                        formatName(bankIdAuthResponse.surname))
                }
            }
            if (status == MemberStatus.INITIATED) {
                applyChain = applyChain!!.andThenApply { MemberStartedOnBoardingEvent(id, MemberStatus.ONBOARDING) }
            }
        }
        val authenticatedEvent = MemberAuthenticatedEvent(id, bankIdAuthResponse.referenceToken)
        if (applyChain != null) {
            applyChain.andThenApply(Supplier<Any> { authenticatedEvent })
        } else {
            apply(authenticatedEvent)
        }
    }

    private fun getBirthdateFromSwedishSsnAndApplyEvent(ssn: String?, applyChain: ApplyMore?): ApplyMore? {
        var applyChain = applyChain
        val birthDate = getBirthdateFromSwedishSsn(ssn)
        if (birthDate != null) {
            if (applyChain != null) {
                applyChain = applyChain.andThenApply(
                    Supplier<Any> { BirthDateUpdatedEvent(id, birthDate) }
                )
                return applyChain
            }
            apply(BirthDateUpdatedEvent(id, birthDate))
        }
        return applyChain
    }

    private fun generateTrackingId() {
        apply(TrackingIdCreatedEvent(id, uuidGenerator.generateRandom()))
    }

    private fun formatName(name: String): String {
        val lowercase = name.toLowerCase()
        return Character.toUpperCase(lowercase[0]).toString() + lowercase.substring(1)
    }

    @Throws(RuntimeException::class)
    private fun getPersonInformationFromBisnode(applyChain: ApplyMore, ssn: String): ApplyMore? {
        var chain = applyChain
        logger.info("Calling bisnode for person information for {}", ssn)
        val personList = bisnodeClient.match(ssn).persons
        val person = personList[0].person
        if (personList.size != 1) {
            throw RuntimeException("Could not find person at bisnode.")
        }
        chain = chain.andThenApply { NameUpdatedEvent(id, person.preferredOrFirstName, person.familyName) }
        val bisnodeInformation = BisnodeInformation(ssn, person)
        if (bisnodeInformation.address.isPresent) {
            chain = chain.andThenApply(
                Supplier<Any> { LivingAddressUpdatedEvent(id, bisnodeInformation.address.get()) })
        }
        chain.andThenApply({ PersonInformationFromBisnodeEvent(id, bisnodeInformation) })
        return chain
    }

    @CommandHandler
    fun handle(command: InactivateMemberCommand) {
        if (status == MemberStatus.INITIATED || status == MemberStatus.ONBOARDING) {
            apply(MemberInactivatedEvent(id))
        } else {
            val str = String.format("Cannot INACTIAVTE member %s in status: %s", id, status.name)
            throw RuntimeException(str)
        }
    }

    @CommandHandler
    fun handle(command: StartSwedishOnboardingWithSSNCommand) {
        if (status == MemberStatus.INITIATED || status == MemberStatus.ONBOARDING) {
            apply(OnboardingStartedWithSSNEvent(id, command.ssn, SSNUpdatedEvent.Nationality.SWEDEN))
            getBirthdateFromSwedishSsnAndApplyEvent(command.ssn, null)
            apply(MemberStartedOnBoardingEvent(id, MemberStatus.ONBOARDING))
        } else {
            throw RuntimeException(String.format("Cannot start onboarding in state: %s", status))
        }
    }

    private fun maybeApplyNameUpdatedEvent(firstName: String?, lastName: String?) {
        val shouldUpdateFirstName = firstName != null && member.firstName != firstName
        val shouldUpdateLastName = lastName != null && member.lastName != lastName

        if (shouldUpdateFirstName || shouldUpdateLastName) {
            apply(NameUpdatedEvent(
                id,
                if (shouldUpdateFirstName) firstName else member.firstName,
                if (shouldUpdateLastName) lastName else member.lastName
            ))
        }
    }

    @CommandHandler
    fun handle(cmd: MemberUpdateContactInformationCommand) {
        if (status == MemberStatus.SIGNED) {
            logger.error("Will not update member info since member is SIGNED (memberId=$id)")
            return
        }
        maybeApplyNameUpdatedEvent(cmd.firstName, cmd.lastName)
        if (cmd.email != null && member.email != cmd.email) {
            apply(EmailUpdatedEvent(id, cmd.email))
        }
        val address = member.livingAddress
        if (address == null
            || address.needsUpdate(
                cmd.street,
                cmd.city,
                cmd.zipCode,
                cmd.apartmentNo,
                cmd.floor)) {
            val floor = if (cmd.floor != null) cmd.floor else 0
            apply(
                LivingAddressUpdatedEvent(
                    id,
                    cmd.street,
                    cmd.city,
                    cmd.zipCode,
                    cmd.apartmentNo,
                    floor))
        }
        if (cmd.phoneNumber != null
            && member.phoneNumber != cmd.phoneNumber) {
            apply(PhoneNumberUpdatedEvent(id, cmd.phoneNumber))
        }
        if (cmd.birthDate != null
            && member.birthDate != cmd.birthDate) {
            apply(BirthDateUpdatedEvent(id, cmd.birthDate))
        }
    }

    @CommandHandler
    fun handle(cmd: SwedishBankIdSignCommand) {
        if (cmd.personalNumber != null
            && member.ssn != cmd.personalNumber) {
            apply(SSNUpdatedEvent(id, cmd.personalNumber, SSNUpdatedEvent.Nationality.SWEDEN))
        }
        getBirthdateFromSwedishSsnAndApplyEvent(cmd.personalNumber, null)
        apply(NewCashbackSelectedEvent(id, cashbackService.getDefaultId(id).toString()))
        apply(
            MemberSignedEvent(
                id, cmd.referenceId, cmd.signature, cmd.oscpResponse,
                cmd.personalNumber))
        if (trackingId == null) {
            generateTrackingId()
        }
    }

    @CommandHandler
    fun handle(cmd: ZignSecSignCommand) {
        if (!isValidJSON(cmd.provideJsonResponse)) throw RuntimeException("Invalid json from provider")
        apply(NewCashbackSelectedEvent(id, cashbackService.getDefaultId(id).toString()))
        when (cmd.zignSecAuthMarket) {
            ZignSecAuthenticationMarket.NORWAY -> {
                when {
                    member.ssn == null ->
                     apply(NorwegianSSNUpdatedEvent(id, cmd.personalNumber))
                    member.ssn != cmd.personalNumber ->
                        logger.warn("Handling `ZignSecSignCommand` with different ssn [member ssn: ${member.ssn}, cmd personalNumber: ${cmd.personalNumber}]")
                }
                apply(
                    MemberIdentifiedEvent(
                        cmd.id,
                        MemberIdentifiedEvent.NationalIdentification(
                            cmd.personalNumber,
                            when (cmd.zignSecAuthMarket) {
                                ZignSecAuthenticationMarket.NORWAY -> MemberIdentifiedEvent.Nationality.NORWAY
                                ZignSecAuthenticationMarket.DENMARK -> MemberIdentifiedEvent.Nationality.DENMARK
                            }
                        ),
                        cmd.zignSecAuthMarket.toMemberIdentifiedEventIdentificationMethod(),
                        cmd.firstName,
                        cmd.lastName
                    )
                )
                apply(
                    NorwegianMemberSignedEvent(
                        id, cmd.personalNumber, cmd.provideJsonResponse, cmd.referenceId))
            }
            ZignSecAuthenticationMarket.DENMARK -> {
                if (cmd.personalNumber != null
                    && member.ssn != cmd.personalNumber) {
                    apply(DanishSSNUpdatedEvent(id, cmd.personalNumber))
                }
                apply(
                    DanishMemberSignedEvent(
                        id, cmd.personalNumber, cmd.provideJsonResponse, cmd.referenceId))
            }
        }
    }

    @CommandHandler
    fun handle(command: ZignSecSuccessfulAuthenticationCommand) {
        apply(
            MemberIdentifiedEvent(
                command.id,
                MemberIdentifiedEvent.NationalIdentification(
                    command.personalNumber,
                    when (command.zignSecAuthMarket) {
                        ZignSecAuthenticationMarket.NORWAY -> MemberIdentifiedEvent.Nationality.NORWAY
                        ZignSecAuthenticationMarket.DENMARK -> MemberIdentifiedEvent.Nationality.DENMARK
                    }
                ),
                command.zignSecAuthMarket.toMemberIdentifiedEventIdentificationMethod(),
                command.firstName,
                command.lastName
            )
        )
    }

    fun isValidJSON(json: String): Boolean {
        return try {
            objectMapper.readTree(json)
            true
        } catch (e: IOException) {
            logger.error("Failed to validate json", e)
            false
        }
    }

    @CommandHandler
    fun handle(command: SignMemberFromUnderwriterCommand) {
        apply(MemberSignedWithoutBankId(command.id, command.ssn))
    }

    @CommandHandler
    fun handle(cmd: MemberSimpleSignedCommand) {
        val identification = cmd.nationalIdentification.identification
        val nationality = cmd.nationalIdentification.nationality
        apply(MemberSimpleSignedEvent(cmd.id, identification, toMemberSimpleSignedEventNationality(nationality), cmd.referenceId))
        apply(SSNUpdatedEvent(id, identification, toSSNUpdatedEventNationality(nationality)))
    }

    @CommandHandler
    fun handle(cmd: SelectNewCashbackCommand) {
        apply(NewCashbackSelectedEvent(id, cmd.optionId.toString()))
    }

    @CommandHandler
    fun handle(cmd: UpdateEmailCommand) {
        apply(EmailUpdatedEvent(id, cmd.email))
    }

    @CommandHandler
    fun handle(cmd: EditMemberInformationCommand) {
        maybeApplyNameUpdatedEvent(cmd.member.firstName, cmd.member.lastName)
        if (cmd.member.email != null
            && member.email != cmd.member.email) {
            apply(EmailUpdatedEvent(id, cmd.member.email), MetaData.with("token", cmd.token))
        }
        val address = member.livingAddress
        if (address == null
            || address.needsUpdate(
                cmd.member.street,
                cmd.member.city,
                cmd.member.zipCode,
                cmd.member.apartment,
                cmd.member.floor)) {
            apply(
                LivingAddressUpdatedEvent(
                    id,
                    cmd.member.street,
                    cmd.member.city,
                    cmd.member.zipCode,
                    cmd.member.apartment,
                    cmd.member.floor),
                MetaData.with("token", cmd.token)
            )
        }
        if (cmd.member.phoneNumber != null
            && member.phoneNumber != cmd.member.phoneNumber) {
            apply(PhoneNumberUpdatedEvent(id, cmd.member.phoneNumber), MetaData.with("token", cmd.token))
        }
    }

    @CommandHandler
    fun handle(cmd: EditMemberInfoCommand) {
        maybeApplyNameUpdatedEvent(cmd.firstName, cmd.lastName)
        if (cmd.email != null && cmd.email != member.email) {
            apply(EmailUpdatedEvent(id, cmd.email), MetaData.with("token", cmd.token))
        }
        if (cmd.phoneNumber != null && cmd.phoneNumber != member.phoneNumber) {
            apply(PhoneNumberUpdatedEvent(id, cmd.phoneNumber), MetaData.with("token", cmd.token))
        }
    }

    @CommandHandler
    fun handle(cmd: UpdatePhoneNumberCommand) {
        logger.info("Updating phoneNumber for member {}, new number: {}", cmd.memberId,
            cmd.phoneNumber)
        if (cmd.phoneNumber != null
            && member.phoneNumber != cmd.phoneNumber) {
            apply(PhoneNumberUpdatedEvent(cmd.memberId, cmd.phoneNumber))
        }
    }

    @CommandHandler
    fun handle(cmd: SetFraudulentStatusCommand) {
        apply(FraudulentStatusUpdatedEvent(cmd.memberId, cmd.fraudulentStatus, cmd.fraudulentDescription), MetaData.with("token", cmd.token))
    }

    @CommandHandler
    fun handle(cmd: UpdateSwedishWebOnBoardingInfoCommand) {
        logger.debug("Updating ssn and email for webOnBoarding member {}, ssn: {}, email: {}",
            cmd.memberId, cmd.ssn, cmd.email)
        if (cmd.ssn != null
            && member.ssn != cmd.ssn) {
            apply(SSNUpdatedEvent(id, cmd.ssn, SSNUpdatedEvent.Nationality.SWEDEN))
            getBirthdateFromSwedishSsnAndApplyEvent(cmd.ssn, null)
        }
        if (cmd.email != null
            && member.email != cmd.email) {
            apply(EmailUpdatedEvent(id, cmd.email))
        }
    }

    @CommandHandler
    fun handle(cmd: UpdateSSNCommand) {
        logger.debug("Updating ssn for member {}, ssn: {}", cmd.memberId, cmd.ssn)
        apply(SSNUpdatedEvent(cmd.memberId, cmd.ssn, toSSNUpdatedEventNationality(cmd.nationality)))
        getBirthdateFromSwedishSsnAndApplyEvent(cmd.ssn, null)
    }

    @CommandHandler
    fun handle(cmd: InitializeAppleUserCommand) {
        apply(
            MemberCreatedEvent(
                cmd.memberId,
                MemberStatus.SIGNED)
        )
        apply(
            SSNUpdatedEvent(
                cmd.memberId,
                cmd.personalNumber,
                SSNUpdatedEvent.Nationality.SWEDEN
            )
        )
        getBirthdateFromSwedishSsnAndApplyEvent(cmd.personalNumber, null)
        apply(
            NewCashbackSelectedEvent(
                cmd.memberId,
                cashbackService.getDefaultId(id).toString())
        )
        apply(
            NameUpdatedEvent(
                cmd.memberId,
                cmd.firstName,
                cmd.lastName),
            null
        )
        apply(
            PhoneNumberUpdatedEvent(
                cmd.memberId,
                cmd.phoneNumber
            )
        )
        apply(
            EmailUpdatedEvent(
                cmd.memberId,
                cmd.email)
        )
        apply(LivingAddressUpdatedEvent(
            cmd.memberId,
            cmd.street,
            cmd.city,
            cmd.zipCode,
            null,
            0
        )
        )
    }

    @CommandHandler
    fun handle(cmd: UpdateAcceptLanguageCommand) {
        logger.info("Updating accept language for member {}, new number: {}", cmd.memberId, cmd.acceptLanguage)
        if (!cmd.acceptLanguage.isEmpty() &&
            member.acceptLanguage != cmd.acceptLanguage) {
            apply(AcceptLanguageUpdatedEvent(cmd.memberId, cmd.acceptLanguage))
        }
    }

    @CommandHandler
    fun handle(cmd: UpdatePickedLocaleCommand) {
        if (member.pickedLocale != cmd.pickedLocale) {
            logger.info("Updating picked locale for member {}, new locale: {}", cmd.memberId, cmd.pickedLocale)
            apply(PickedLocaleUpdatedEvent(cmd.memberId, cmd.pickedLocale))
        }
    }

    @CommandHandler
    fun handle(cmd: UpdateBirthDateCommand) {
        apply(BirthDateUpdatedEvent(cmd.memberId, cmd.birthDate))
    }

    @CommandHandler
    fun handle(cmd: MemberIdentifiedCommand) {
        apply(
            MemberIdentifiedEvent(
                cmd.id,
                cmd.nationalIdentification,
                cmd.identificationMethod,
                cmd.firstName,
                cmd.lastName
            )
        )
    }

    @EventSourcingHandler
    fun on(e: MemberCreatedEvent) {
        _id = e.id
        status = e.status
        member = Member()
    }

    @EventSourcingHandler
    fun on(e: MemberStartedOnBoardingEvent) {
        status = e.newStatus
    }

    @EventSourcingHandler
    fun on(e: NameUpdatedEvent) {
        member = member.copy(firstName = e.firstName, lastName = e.lastName)
    }

    @EventSourcingHandler
    fun on(e: PersonInformationFromBisnodeEvent) {
        latestBisnodeInformation = e.information
    }

    @EventSourcingHandler
    fun on(e: MemberSignedEvent) {
        status = MemberStatus.SIGNED
    }

    @EventSourcingHandler
    fun on(e: NorwegianMemberSignedEvent) {
        status = MemberStatus.SIGNED
    }

    @EventSourcingHandler
    fun on(e: DanishMemberSignedEvent) {
        status = MemberStatus.SIGNED
    }

    @EventSourcingHandler
    fun on(e: EmailUpdatedEvent) {
        member = member.copy(email = e.email)
    }

    @EventSourcingHandler
    fun on(e: LivingAddressUpdatedEvent) {
        val address = LivingAddress(
            e.street, e.city, e.zipCode, e.apartmentNo, e.floor)
        member = member.copy(livingAddress = address)
    }

    @EventSourcingHandler
    fun on(e: OnboardingStartedWithSSNEvent) {
        member = member.copy(ssn = e.ssn)
    }

    @EventSourcingHandler
    fun on(e: MemberCancellationEvent) {
        status = MemberStatus.TERMINATED
    }

    @EventSourcingHandler
    fun on(e: InsuranceCancellationEvent) {
        logger.info("Cancel insurance with id {} for member {}", e.insuranceId, e.memberId)
    }

    @EventHandler
    fun on(e: TrackingIdCreatedEvent) {
        trackingId = e.trackingId
    }

    @EventSourcingHandler
    fun on(e: PhoneNumberUpdatedEvent) {
        member = member.copy(phoneNumber = e.phoneNumber)
    }

    @EventSourcingHandler
    fun on(e: BirthDateUpdatedEvent) {
        member = member.copy(birthDate = e.birthDate)
    }

    @EventSourcingHandler
    fun on(e: SSNUpdatedEvent) {
        member = member.copy(ssn = e.ssn)
    }

    @EventSourcingHandler
    fun on(e: NorwegianSSNUpdatedEvent) {
        member = member.copy(ssn = e.ssn)
    }

    @EventSourcingHandler
    fun on(e: DanishSSNUpdatedEvent) {
        member = member.copy(ssn = e.ssn)
    }

    @EventSourcingHandler
    fun on(e: AcceptLanguageUpdatedEvent) {
        member = member.copy(acceptLanguage = e.acceptLanguage)
    }

    @EventSourcingHandler
    fun on(e: PickedLocaleUpdatedEvent) {
        member = member.copy(pickedLocale = e.pickedLocale)
    }
}
