package com.hedvig.memberservice

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.common.UUIDGenerator
import com.hedvig.external.bisnodeBCI.BisnodeClient
import com.hedvig.memberservice.aggregates.MemberAggregate
import com.hedvig.memberservice.aggregates.MemberStatus
import com.hedvig.memberservice.aggregates.PickedLocale
import com.hedvig.memberservice.commands.BankIdAuthenticationStatus
import com.hedvig.memberservice.commands.InactivateMemberCommand
import com.hedvig.memberservice.commands.MemberSimpleSignedCommand
import com.hedvig.memberservice.commands.MemberUpdateContactInformationCommand
import com.hedvig.memberservice.commands.PopulateMemberThroughLoginDataCommand
import com.hedvig.memberservice.commands.SelectNewCashbackCommand
import com.hedvig.memberservice.commands.StartSwedishOnboardingWithSSNCommand
import com.hedvig.memberservice.commands.SwedishBankIdAuthenticationAttemptCommand
import com.hedvig.memberservice.commands.SwedishBankIdSignCommand
import com.hedvig.memberservice.commands.UpdateBirthDateCommand
import com.hedvig.memberservice.commands.UpdatePickedLocaleCommand
import com.hedvig.memberservice.commands.UpdateSSNCommand
import com.hedvig.memberservice.commands.UpdateSwedishWebOnBoardingInfoCommand
import com.hedvig.memberservice.commands.ZignSecSignCommand
import com.hedvig.memberservice.commands.ZignSecSuccessfulAuthenticationCommand
import com.hedvig.memberservice.commands.models.ZignSecAuthenticationMarket
import com.hedvig.memberservice.events.BirthDateUpdatedEvent
import com.hedvig.memberservice.events.DanishMemberSignedEvent
import com.hedvig.memberservice.events.DanishSSNUpdatedEvent
import com.hedvig.memberservice.events.EmailUpdatedEvent
import com.hedvig.memberservice.events.LivingAddressUpdatedEvent
import com.hedvig.memberservice.events.MemberAuthenticatedEvent
import com.hedvig.memberservice.events.MemberCreatedEvent
import com.hedvig.memberservice.events.MemberInactivatedEvent
import com.hedvig.memberservice.events.MemberSignedEvent
import com.hedvig.memberservice.events.MemberSimpleSignedEvent
import com.hedvig.memberservice.events.MemberStartedOnBoardingEvent
import com.hedvig.memberservice.events.NameUpdatedEvent
import com.hedvig.memberservice.events.NewCashbackSelectedEvent
import com.hedvig.memberservice.events.NorwegianMemberSignedEvent
import com.hedvig.memberservice.events.NorwegianSSNUpdatedEvent
import com.hedvig.memberservice.events.OnboardingStartedWithSSNEvent
import com.hedvig.memberservice.events.PickedLocaleUpdatedEvent
import com.hedvig.memberservice.events.SSNUpdatedEvent
import com.hedvig.memberservice.events.TrackingIdCreatedEvent
import com.hedvig.memberservice.events.MemberIdentifiedEvent
import com.hedvig.memberservice.services.cashback.CashbackService
import com.hedvig.memberservice.web.dto.Address
import com.hedvig.memberservice.web.dto.NationalIdentification
import com.hedvig.memberservice.web.dto.Nationality
import com.hedvig.memberservice.web.dto.StartOnboardingWithSSNRequest
import com.hedvig.memberservice.web.dto.UpdateContactInformationRequest
import io.mockk.every
import io.mockk.mockk
import org.axonframework.eventsourcing.AbstractAggregateFactory
import org.axonframework.eventsourcing.DomainEventMessage
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.client.RestClientException
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@RunWith(SpringRunner::class)
class MemberAggregateTests {
    lateinit var fixture: FixtureConfiguration<MemberAggregate>

    val bisnodeClient = mockk<BisnodeClient>()

    val cashbackService = mockk<CashbackService>()

    val uuidGenerator = mockk<UUIDGenerator>()

    val objectMapper: ObjectMapper = ObjectMapper()

    @Before
    fun setUp() {
        fixture = AggregateTestFixture(MemberAggregate::class.java)
        fixture.registerAggregateFactory(AggregateFactoryM(MemberAggregate::class.java))
    }

    @Test
    fun contextLoads() {
        val memberId = 123L
        fixture.given(MemberCreatedEvent(memberId, MemberStatus.INITIATED, Instant.now()))
    }

    @Test
    fun authenticatedAttemptCommand_givenInitiatedMember_emitsManyEvents() {
        val memberId = 1234L
        val referenceTokenValue = "referenceTokenValue"
        every { bisnodeClient.match(TOLVANSSON_SSN) } throws RestClientException("Something went wrong!")
        val uuid = UUID.fromString("971b25bc-5db5-11e8-9f7c-039208e9dccf")
        every { uuidGenerator.generateRandom() } returns uuid
        val authStatus = makeBankIdAuthenticationStatus(
            TOLVANSSON_SSN, referenceTokenValue, TOLVANSSON_FIRST_NAME, TOLVANSSON_LAST_NAME)
        val cmd = SwedishBankIdAuthenticationAttemptCommand(memberId, authStatus)
        fixture
            .given(MemberCreatedEvent(memberId, MemberStatus.INITIATED, Instant.now()))
            .`when`(cmd)
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                SSNUpdatedEvent(memberId, TOLVANSSON_SSN, SSNUpdatedEvent.Nationality.SWEDEN),
                TrackingIdCreatedEvent(memberId, uuid),
                BirthDateUpdatedEvent(memberId, LocalDate.of(1912, 12, 12)),
                NameUpdatedEvent(memberId, "Tolvan", "Tolvansson"),
                MemberStartedOnBoardingEvent(memberId, MemberStatus.ONBOARDING),
                MemberAuthenticatedEvent(memberId, referenceTokenValue))
    }

    @Test
    fun authenticationAttemptedCommand_givenSignedMember_OnlyEmitsMemberAuthenticatedEvent() {
        val memberId = 1234L
        val referenceId = "someReferenceId"
        every { uuidGenerator.generateRandom() } returns TRACKING_UUID
        every { cashbackService.getDefaultId(any()) } returns DEFAULT_CASHBACK
        val bankIdAuthStatus = makeBankIdAuthenticationStatus(
            TOLVANSSON_SSN, referenceId, TOLVANSSON_FIRST_NAME, TOLVANSSON_LAST_NAME)
        fixture
            .given(
                MemberCreatedEvent(memberId, MemberStatus.INITIATED, Instant.now()),
                NewCashbackSelectedEvent(memberId, DEFAULT_CASHBACK.toString()),
                MemberSignedEvent(memberId, referenceId, "", "", TOLVANSSON_SSN),
                TrackingIdCreatedEvent(memberId, TRACKING_UUID))
            .`when`(SwedishBankIdAuthenticationAttemptCommand(memberId, bankIdAuthStatus))
            .expectSuccessfulHandlerExecution()
            .expectEvents(MemberAuthenticatedEvent(memberId, referenceId))
    }

    private fun makeBankIdAuthenticationStatus(
        ssn: String, referenceTokenValue: String, firstName: String, lastName: String): BankIdAuthenticationStatus {
        return BankIdAuthenticationStatus(ssn, referenceTokenValue, firstName, lastName)
    }

    @Test
    fun memberUpdatePersonalInformation() {
        val memberId = 1234L
        val address = Address()
        address.street = "Spånga bro"
        address.city = "Spånga"
        address.apartmentNo = "1104"
        address.zipCode = "55748"
        address.floor = 0
        val request = UpdateContactInformationRequest(
            memberId.toString(),
            "Arn",
            "Magnusson",
            "email@hedvig.com",
            null,
            address,
            null
        )

        fixture
            .given(MemberCreatedEvent(memberId, MemberStatus.INITIATED, Instant.now()))
            .`when`(MemberUpdateContactInformationCommand(memberId, request))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                NameUpdatedEvent(memberId, request.firstName!!, request.lastName!!),
                EmailUpdatedEvent(memberId, "email@hedvig.com"),
                LivingAddressUpdatedEvent(
                    memberId,
                    address.street,
                    address.city,
                    address.zipCode,
                    address.apartmentNo,
                    0))
    }

    @Test
    fun startOnBoardingFromSSN() {
        val memberId = 1234L
        val ssn = "192005059999"
        val request = StartOnboardingWithSSNRequest(ssn)
        fixture
            .given(MemberCreatedEvent(memberId, MemberStatus.INITIATED, Instant.now()))
            .`when`(StartSwedishOnboardingWithSSNCommand(memberId, request))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                OnboardingStartedWithSSNEvent(memberId, ssn, SSNUpdatedEvent.Nationality.SWEDEN),
                BirthDateUpdatedEvent(memberId, LocalDate.of(1920, 5, 5)),
                MemberStartedOnBoardingEvent(memberId, MemberStatus.ONBOARDING))
    }

    @Test
    fun selectNewCashbackCommand_thenReturnNewCashbackSelectedEvent() {
        val memberId = 1234L
        val cashbackId = "328354a4-d119-11e7-ac68-139bd471ea9a"
        fixture
            .given(MemberCreatedEvent(memberId, MemberStatus.INITIATED, Instant.now()))
            .`when`(SelectNewCashbackCommand(memberId, UUID.fromString(cashbackId)))
            .expectSuccessfulHandlerExecution()
            .expectEvents(NewCashbackSelectedEvent(memberId, cashbackId))
    }

    @Test
    fun bankIdSignCommand_givenMemberWhoHasNotAuthed_ThenDoEmitTrackingIdEvent() {
        val memberId = 1234L
        val referenceId = "someReferenceId"
        val personalNumber = "198902171234"
        every { uuidGenerator.generateRandom() } returns TRACKING_UUID
        every { cashbackService.getDefaultId(any()) } returns DEFAULT_CASHBACK
        fixture
            .given(MemberCreatedEvent(memberId, MemberStatus.INITIATED, Instant.now()))
            .`when`(SwedishBankIdSignCommand(memberId, referenceId, "", "", personalNumber))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                SSNUpdatedEvent(memberId, personalNumber, SSNUpdatedEvent.Nationality.SWEDEN),
                BirthDateUpdatedEvent(memberId, LocalDate.of(1989, 2, 17)),
                NewCashbackSelectedEvent(memberId, DEFAULT_CASHBACK.toString()),
                MemberSignedEvent(memberId, referenceId, "", "", personalNumber),
                TrackingIdCreatedEvent(memberId, TRACKING_UUID))
    }

    @Test
    fun bankIdSignCommand_givenMemberWhoHasAuthenticated_ThenDoNotEmitTrackingIdEvent() {
        val memberId = 1234L
        val referenceId = "someReferenceId"
        val personalNumber = "198902171234"
        every { uuidGenerator.generateRandom() } returns TRACKING_UUID
        every { cashbackService.getDefaultId(any()) } returns DEFAULT_CASHBACK
        fixture
            .given(
                MemberCreatedEvent(memberId, MemberStatus.INITIATED, Instant.now()),
                MemberAuthenticatedEvent(memberId, referenceId),
                MemberStartedOnBoardingEvent(memberId, MemberStatus.ONBOARDING),
                TrackingIdCreatedEvent(memberId, TRACKING_UUID))
            .`when`(SwedishBankIdSignCommand(memberId, referenceId, "", "", personalNumber))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                SSNUpdatedEvent(memberId, personalNumber, SSNUpdatedEvent.Nationality.SWEDEN),
                BirthDateUpdatedEvent(memberId, LocalDate.of(1989, 2, 17)),
                NewCashbackSelectedEvent(memberId, DEFAULT_CASHBACK.toString()),
                MemberSignedEvent(memberId, referenceId, "", "", personalNumber)
            )
    }

    @Test
    fun norwegianSignCommand_validJSON_ThenEmitThreeEvents() {
        val memberId = 1234L
        val referenceId = UUID.randomUUID()
        val personalNumber = "12121212120"
        val provideJsonResponse = "{ \"json\": true }"
        every { cashbackService.getDefaultId(memberId) } returns DEFAULT_CASHBACK
        fixture
            .given(
                MemberCreatedEvent(memberId, MemberStatus.INITIATED, Instant.now()),
                MemberStartedOnBoardingEvent(memberId, MemberStatus.ONBOARDING),
                TrackingIdCreatedEvent(memberId, TRACKING_UUID))
            .`when`(ZignSecSignCommand(memberId, referenceId, personalNumber, provideJsonResponse, ZignSecAuthenticationMarket.NORWAY, "Test", "Testsson"))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                NewCashbackSelectedEvent(memberId, DEFAULT_CASHBACK.toString()),
                NorwegianSSNUpdatedEvent(memberId, personalNumber),
                 MemberIdentifiedEvent(
                    memberId,
                    MemberIdentifiedEvent.NationalIdentification(personalNumber, MemberIdentifiedEvent.Nationality.NORWAY),
                    MemberIdentifiedEvent.IdentificationMethod.NORWEGIAN_BANK_ID,
                    "Test",
                    "Testsson"
                ),
                NorwegianMemberSignedEvent(memberId, personalNumber, provideJsonResponse, referenceId)
            )
    }

    @Test
    fun danishSignCommand_validJSON_ThenEmitThreeEvents() {
        val memberId = 1234L
        val referenceId = UUID.randomUUID()
        val personalNumber = "1212121212"
        val provideJsonResponse = "{ \"json\": true }"
        every { cashbackService.getDefaultId(any()) } returns DEFAULT_CASHBACK
        fixture
            .given(
                MemberCreatedEvent(memberId, MemberStatus.INITIATED, Instant.now()),
                MemberStartedOnBoardingEvent(memberId, MemberStatus.ONBOARDING),
                TrackingIdCreatedEvent(memberId, TRACKING_UUID))
            .`when`(ZignSecSignCommand(memberId, referenceId, personalNumber, provideJsonResponse, ZignSecAuthenticationMarket.DENMARK, "Test", "Testsson"))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                NewCashbackSelectedEvent(memberId, DEFAULT_CASHBACK.toString()),
                DanishSSNUpdatedEvent(memberId, personalNumber),
                MemberIdentifiedEvent(
                    memberId,
                    MemberIdentifiedEvent.NationalIdentification(personalNumber, MemberIdentifiedEvent.Nationality.DENMARK),
                    MemberIdentifiedEvent.IdentificationMethod.DANISH_BANK_ID,
                    "Test",
                    "Testsson"
                ),
                DanishMemberSignedEvent(memberId, personalNumber, provideJsonResponse, referenceId)
            )
    }

    @Test
    fun norwegianSignCommand_invalidJSON_expectException() {
        val memberId = 1234L
        val referenceId = UUID.randomUUID()
        val personalNumber = "12121212120"
        val provideJsonResponse = "not a valid json"
        every { cashbackService.getDefaultId(any()) } returns DEFAULT_CASHBACK
        fixture
            .given(
                MemberCreatedEvent(memberId, MemberStatus.INITIATED, Instant.now()),
                MemberStartedOnBoardingEvent(memberId, MemberStatus.ONBOARDING),
                TrackingIdCreatedEvent(memberId, TRACKING_UUID))
            .`when`(ZignSecSignCommand(memberId, referenceId, personalNumber, provideJsonResponse, ZignSecAuthenticationMarket.NORWAY, null, null))
            .expectException(RuntimeException::class.java)
    }

    @Test
    fun inactivateMemberCommand_givenInitiatedMember_thenEmitsMemberInactivatedEvent() {
        val memberId = 1234L
        fixture
            .given(MemberCreatedEvent(memberId, MemberStatus.INITIATED, Instant.now()))
            .`when`(InactivateMemberCommand(memberId))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                MemberInactivatedEvent(memberId))
    }

    @Test
    fun inactivateMemberCommand_givenOnboardingMember_thenEmitsMemberInactivatedEvent() {
        val memberId = 1234L
        fixture
            .given(MemberCreatedEvent(memberId, MemberStatus.INITIATED, Instant.now()),
                MemberStartedOnBoardingEvent(memberId, MemberStatus.ONBOARDING))
            .`when`(InactivateMemberCommand(memberId))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                MemberInactivatedEvent(memberId))
    }

    @Test
    fun pickedLocaleUpdateCommand_givenNotSigned_thenUpdatePickedLocale() {
        val memberId = 1234L
        fixture
            .given(MemberCreatedEvent(memberId, MemberStatus.INITIATED, Instant.now()))
            .`when`(UpdatePickedLocaleCommand(memberId, PickedLocale.sv_SE))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                PickedLocaleUpdatedEvent(memberId, PickedLocale.sv_SE)
            )
    }

    @Test
    fun pickedLocaleUpdateCommand_givenSigned_thenExpectUpdatePickedLocaleEvents() {
        val memberId = 1234L
        val referenceId = "someReferenceId"
        val personalNumber = "198902171234"
        every { uuidGenerator!!.generateRandom() } returns TRACKING_UUID
        every { cashbackService.getDefaultId(any()) } returns DEFAULT_CASHBACK
        fixture
            .given(
                MemberCreatedEvent(memberId, MemberStatus.INITIATED, Instant.now()),
                SSNUpdatedEvent(memberId, personalNumber, SSNUpdatedEvent.Nationality.SWEDEN),
                NewCashbackSelectedEvent(memberId, DEFAULT_CASHBACK.toString()),
                MemberSignedEvent(memberId, referenceId, "", "", personalNumber),
                PickedLocaleUpdatedEvent(memberId, PickedLocale.sv_SE),
                TrackingIdCreatedEvent(memberId, TRACKING_UUID)
            )
            .`when`(UpdatePickedLocaleCommand(memberId, PickedLocale.nb_NO))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                PickedLocaleUpdatedEvent(memberId, PickedLocale.nb_NO)
            )
    }

    @Test
    fun updateSSNCommand_whenMemberIsCreated_shouldUpdateSsnAndBirthdate() {
        val memberId = 1234L
        val personalNumber = "198902171234"
        fixture
            .given(
                MemberCreatedEvent(memberId, MemberStatus.INITIATED, Instant.now())
            )
            .`when`(UpdateSSNCommand(memberId, personalNumber, Nationality.SWEDEN))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                SSNUpdatedEvent(memberId, personalNumber, SSNUpdatedEvent.Nationality.SWEDEN),
                BirthDateUpdatedEvent(memberId, LocalDate.of(1989, 2, 17))
            )
    }

    @Test
    fun updateWebOnBoardingInfoCommand_whenMemberIsCreated_shouldUpdateSsnAndBirthdateAndEmail() {
        val memberId = 1234L
        val personalNumber = "198902171234"
        val email = "em@i.l"
        fixture
            .given(
                MemberCreatedEvent(memberId, MemberStatus.INITIATED, Instant.now())
            )
            .`when`(UpdateSwedishWebOnBoardingInfoCommand(memberId, personalNumber, email))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                SSNUpdatedEvent(memberId, personalNumber, SSNUpdatedEvent.Nationality.SWEDEN),
                BirthDateUpdatedEvent(memberId, LocalDate.of(1989, 2, 17)),
                EmailUpdatedEvent(memberId, email)
            )
    }

    @Test
    fun memberSimpleSignedCommand_whenMemberIsCreated_shouldSignMemberAndUpdateSsn() {
        val memberId = 1234L
        val personalNumber = "198902171234"
        val nationalIdentification = NationalIdentification(personalNumber, Nationality.SWEDEN)
        val refId = UUID.randomUUID()
        every { cashbackService.getDefaultId(any()) } returns DEFAULT_CASHBACK
        fixture
            .given(
                MemberCreatedEvent(memberId, MemberStatus.INITIATED, Instant.now())
            )
            .`when`(MemberSimpleSignedCommand(memberId, nationalIdentification, refId))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                NewCashbackSelectedEvent(memberId, DEFAULT_CASHBACK.toString()),
                MemberSimpleSignedEvent(memberId, personalNumber, MemberSimpleSignedEvent.Nationality.SWEDEN, refId),
                SSNUpdatedEvent(memberId, personalNumber, SSNUpdatedEvent.Nationality.SWEDEN)
            )
    }

    @Test
    fun updateBirthDateCommand_whenMemberIsCreated_should() {
        val memberId = 1234L
        val birthDate = LocalDate.of(1978, 2, 3)
        fixture
            .given(
                MemberCreatedEvent(memberId, MemberStatus.INITIATED, Instant.now())
            )
            .`when`(UpdateBirthDateCommand(memberId, birthDate))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                BirthDateUpdatedEvent(memberId, birthDate)
            )
    }

    @Test
    fun `handle valid norwegian ZignSecSuccessfulAuthenticationCommand should apply ZignSecSuccessfulAuthenticationEvent`() {
        val memberId = 1234L
        val referenceId = UUID.randomUUID()
        val personalNumber = "12121212120"
        fixture
            .given(
                MemberCreatedEvent(memberId, MemberStatus.INITIATED, Instant.now())
            )
            .`when`(ZignSecSuccessfulAuthenticationCommand(
                memberId,
                referenceId,
                personalNumber,
                ZignSecAuthenticationMarket.NORWAY,
                "Test",
                "Testsson"
            ))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                MemberIdentifiedEvent(
                    memberId,
                    MemberIdentifiedEvent.NationalIdentification(
                        personalNumber,
                        MemberIdentifiedEvent.Nationality.NORWAY
                    ),
                    MemberIdentifiedEvent.IdentificationMethod.NORWEGIAN_BANK_ID,
                    "Test",
                    "Testsson"
                )
            )
    }

    @Test
    fun `handle valid danish ZignSecSuccessfulAuthenticationCommand should apply ZignSecSuccessfulAuthenticationEvent`() {
        val memberId = 1234L
        val referenceId = UUID.randomUUID()
        val personalNumber = "12121212120"
        fixture
            .given(
                MemberCreatedEvent(memberId, MemberStatus.INITIATED, Instant.now())
            )
            .`when`(ZignSecSuccessfulAuthenticationCommand(
                memberId,
                referenceId,
                personalNumber,
                ZignSecAuthenticationMarket.DENMARK,
                "Test",
                "Testsson"
            ))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                MemberIdentifiedEvent(
                    memberId,
                    MemberIdentifiedEvent.NationalIdentification(
                        personalNumber,
                        MemberIdentifiedEvent.Nationality.DENMARK
                    ),
                    MemberIdentifiedEvent.IdentificationMethod.DANISH_BANK_ID,
                    "Test",
                    "Testsson"
                )
            )
    }

    @Test
    fun `empty member - information is populated through login command`() {
        val memberId = 1234L
        fixture
            .given(
                MemberCreatedEvent(memberId, MemberStatus.INITIATED, Instant.now())
            )
            .`when`(
                PopulateMemberThroughLoginDataCommand(memberId, "First", "Lastname")
            )
            .expectEvents(
                NameUpdatedEvent(memberId, "First", "Lastname")
            )
    }

    @Test
    fun `empty member - information is not populated through login command if the data is the same`() {
        val memberId = 1234L
        fixture
            .given(
                MemberCreatedEvent(memberId, MemberStatus.INITIATED, Instant.now()),
                NameUpdatedEvent(memberId, "First", "Lastname")
            )
            .`when`(
                PopulateMemberThroughLoginDataCommand(memberId, "First", "Lastname")
            )
            .expectNoEvents()
    }

    private inner class AggregateFactoryM<T> constructor(aggregateType: Class<T>?) : AbstractAggregateFactory<T>(aggregateType) {
        override fun doCreateAggregate(aggregateIdentifier: String, firstEvent: DomainEventMessage<*>?): T {
            val m = MemberAggregate()
            m.cashbackService = cashbackService
            m.bisnodeClient = bisnodeClient
            m.uuidGenerator = uuidGenerator
            m.objectMapper = objectMapper
            return m as T
        }
    }

    companion object {
        private val DEFAULT_CASHBACK = UUID.fromString("9881f632-fb69-11e7-9238-a39b7922d42d")
        private val TRACKING_UUID = UUID.fromString("971b25bc-5db5-11e8-9f7c-039208e9dccf")
        private const val TOLVANSSON_SSN = "191212121212"
        private const val TOLVANSSON_FIRST_NAME = "TOLVAN"
        private const val TOLVANSSON_LAST_NAME = "TOLVANSSON"
    }
}
