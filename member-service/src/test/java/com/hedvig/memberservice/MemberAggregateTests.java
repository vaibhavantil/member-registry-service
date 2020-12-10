package com.hedvig.memberservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.common.UUIDGenerator;
import com.hedvig.external.bisnodeBCI.BisnodeClient;
import com.hedvig.memberservice.aggregates.BisnodeAddress;
import com.hedvig.memberservice.aggregates.MemberAggregate;
import com.hedvig.memberservice.aggregates.MemberStatus;
import com.hedvig.memberservice.aggregates.PickedLocale;
import com.hedvig.memberservice.commands.*;
import com.hedvig.memberservice.commands.models.ZignSecAuthenticationMarket;
import com.hedvig.memberservice.events.*;
import com.hedvig.memberservice.services.cashback.CashbackService;
import com.hedvig.memberservice.web.dto.Address;
import com.hedvig.memberservice.web.dto.StartOnboardingWithSSNRequest;
import com.hedvig.memberservice.web.dto.UpdateContactInformationRequest;
import lombok.val;
import org.axonframework.eventsourcing.AbstractAggregateFactory;
import org.axonframework.eventsourcing.DomainEventMessage;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class MemberAggregateTests {

    private static final UUID DEFAULT_CASHBACK =
        java.util.UUID.fromString("9881f632-fb69-11e7-9238-a39b7922d42d");
    private static final java.util.UUID TRACKING_UUID =
        java.util.UUID.fromString("971b25bc-5db5-11e8-9f7c-039208e9dccf");
    private static final String TOLVANSSON_SSN = "191212121212";
    private static final String TOLVANSSON_FIRST_NAME = "TOLVAN";
    private static final String TOLVANSSON_LAST_NAME = "TOLVANSSON";
    // public static final java.util.UUID UUID = java.util.UUID
    //    .fromString("971b25bc-5db5-11e8-9f7c-039208e9dccf");
    private FixtureConfiguration<MemberAggregate> fixture;
    @MockBean
    private BisnodeClient bisnodeClient;
    @MockBean
    private CashbackService cashbackService;
    @MockBean
    private UUIDGenerator uuidGenerator;

    ObjectMapper objectMapper;

    @Before
    public void setUp() {
        fixture = new AggregateTestFixture<>(MemberAggregate.class);
        fixture.registerAggregateFactory(new AggregateFactoryM<>(MemberAggregate.class));

        objectMapper = new ObjectMapper();
    }

    @Test
    public void contextLoads() {
        Long memberId = 123L;
        fixture.given(new MemberCreatedEvent(memberId, MemberStatus.INITIATED));
    }

    @Test
    public void authenticatedAttemptCommand_givenInitiatedMember_emitsManyEvents() {
        Long memberId = 1234L;
        final String referenceTokenValue = "referenceTokenValue";

        when(bisnodeClient.match(TOLVANSSON_SSN))
            .thenThrow(new RestClientException("Something went wrong!"));

        val uuid = UUID.fromString("971b25bc-5db5-11e8-9f7c-039208e9dccf");
        when(uuidGenerator.generateRandom()).thenReturn(uuid);

        BankIdAuthenticationStatus authStatus =
            makeBankIdAuthenticationStatus(
                TOLVANSSON_SSN, referenceTokenValue, TOLVANSSON_FIRST_NAME, TOLVANSSON_LAST_NAME);

        AuthenticationAttemptCommand cmd = new AuthenticationAttemptCommand(memberId, authStatus);

        fixture
            .given(new MemberCreatedEvent(memberId, MemberStatus.INITIATED))
            .when(cmd)
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                new SSNUpdatedEvent(memberId, TOLVANSSON_SSN),
                new TrackingIdCreatedEvent(memberId, uuid),
                new BirthDateUpdatedEvent(memberId, LocalDate.of(1912, 12, 12)),
                new NameUpdatedEvent(memberId, "Tolvan", "Tolvansson"),
                new MemberStartedOnBoardingEvent(memberId, MemberStatus.ONBOARDING),
                new MemberAuthenticatedEvent(memberId, referenceTokenValue));
    }

    @Test
    public void authenticationAttemptedCommand_givenSignedMember_OnlyEmitsMemberAuthenticatedEvent() {
        Long memberId = 1234L;
        String referenceId = "someReferenceId";

        when(uuidGenerator.generateRandom()).thenReturn(TRACKING_UUID);

        when(cashbackService.getDefaultId(anyLong())).thenReturn(DEFAULT_CASHBACK);

        val bankIdAuthStatus =
            makeBankIdAuthenticationStatus(
                TOLVANSSON_SSN, referenceId, TOLVANSSON_FIRST_NAME, TOLVANSSON_LAST_NAME);

        fixture
            .given(
                new MemberCreatedEvent(memberId, MemberStatus.INITIATED),
                new NewCashbackSelectedEvent(memberId, DEFAULT_CASHBACK.toString()),
                new MemberSignedEvent(memberId, referenceId, "", "", TOLVANSSON_SSN),
                new TrackingIdCreatedEvent(memberId, TRACKING_UUID))
            .when(new AuthenticationAttemptCommand(memberId, bankIdAuthStatus))
            .expectSuccessfulHandlerExecution()
            .expectEvents(new MemberAuthenticatedEvent(memberId, referenceId));
    }

    private BankIdAuthenticationStatus makeBankIdAuthenticationStatus(
        String ssn, String referenceTokenValue, String firstName, String lastName) {
        return new BankIdAuthenticationStatus(ssn, referenceTokenValue, firstName, lastName);
    }

    @Test
    public void memberUpdatePersonalInformation() {
        Long memberId = 1234L;

        UpdateContactInformationRequest request = new UpdateContactInformationRequest();
        request.setFirstName("Arn");
        request.setLastName("Magnusson");
        request.setEmail("email@hedvig.com");
        Address address = new Address();
        address.setStreet("Spånga bro");
        address.setCity("Spånga");
        address.setApartmentNo("1104");
        address.setZipCode("55748");
        address.setFloor(0);
        request.setAddress(address);

        fixture
            .given(new MemberCreatedEvent(memberId, MemberStatus.INITIATED))
            .when(new MemberUpdateContactInformationCommand(memberId, request))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                new NameUpdatedEvent(memberId, request.getFirstName(), request.getLastName()),
                new EmailUpdatedEvent(memberId, "email@hedvig.com"),
                new LivingAddressUpdatedEvent(
                    memberId,
                    address.getStreet(),
                    address.getCity(),
                    address.getZipCode(),
                    address.getApartmentNo(),
                    0));
    }

    @Test
    public void startOnBoardingFromSSN() {
        Long memberId = 1234L;

        String ssn = "192005059999";
        StartOnboardingWithSSNRequest request = new StartOnboardingWithSSNRequest(ssn);

        fixture
            .given(new MemberCreatedEvent(memberId, MemberStatus.INITIATED))
            .when(new StartOnboardingWithSSNCommand(memberId, request))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                new OnboardingStartedWithSSNEvent(memberId, ssn),
                new BirthDateUpdatedEvent(memberId, LocalDate.of(1920, 5, 5)),
                new MemberStartedOnBoardingEvent(memberId, MemberStatus.ONBOARDING));
    }

    @Test
    public void selectNewCashbackCommand_thenReturnNewCashbackSelectedEvent() {
        Long memberId = 1234L;
        String cashbackId = "328354a4-d119-11e7-ac68-139bd471ea9a";

        fixture
            .given(new MemberCreatedEvent(memberId, MemberStatus.INITIATED))
            .when(new SelectNewCashbackCommand(memberId, UUID.fromString(cashbackId)))
            .expectSuccessfulHandlerExecution()
            .expectEvents(new NewCashbackSelectedEvent(memberId, cashbackId));
    }

    @Test
    public void bankIdSignCommand_givenMemberWhoHasNotAuthed_ThenDoEmitTrackingIdEvent() {
        Long memberId = 1234L;
        String referenceId = "someReferenceId";
        String personalNumber = "198902171234";

        when(uuidGenerator.generateRandom()).thenReturn(TRACKING_UUID);

        when(cashbackService.getDefaultId(anyLong())).thenReturn(DEFAULT_CASHBACK);

        fixture
            .given(new MemberCreatedEvent(memberId, MemberStatus.INITIATED))
            .when(new BankIdSignCommand(memberId, referenceId, "", "", personalNumber))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                new SSNUpdatedEvent(memberId, personalNumber),
                new BirthDateUpdatedEvent(memberId, LocalDate.of(1989, 2, 17)),
                new NewCashbackSelectedEvent(memberId, DEFAULT_CASHBACK.toString()),
                new MemberSignedEvent(memberId, referenceId, "", "", personalNumber),
                new TrackingIdCreatedEvent(memberId, TRACKING_UUID));
    }


    @Test
    public void bankIdSignCommand_givenMemberWhoHasAuthenticated_ThenDoNotEmitTrackingIdEvent() {
        Long memberId = 1234L;
        String referenceId = "someReferenceId";
        String personalNumber = "198902171234";

        when(uuidGenerator.generateRandom()).thenReturn(TRACKING_UUID);

        when(cashbackService.getDefaultId(anyLong())).thenReturn(DEFAULT_CASHBACK);

        fixture
            .given(
                new MemberCreatedEvent(memberId, MemberStatus.INITIATED),
                new MemberAuthenticatedEvent(memberId, referenceId),
                new MemberStartedOnBoardingEvent(memberId, MemberStatus.ONBOARDING),
                new TrackingIdCreatedEvent(memberId, TRACKING_UUID))
            .when(new BankIdSignCommand(memberId, referenceId, "", "", personalNumber))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                new SSNUpdatedEvent(memberId, personalNumber),
                new BirthDateUpdatedEvent(memberId, LocalDate.of(1989, 2, 17)),
                new NewCashbackSelectedEvent(memberId, DEFAULT_CASHBACK.toString()),
                new MemberSignedEvent(memberId, referenceId, "", "", personalNumber)
            );
    }

    @Test
    public void norwegianSignCommand_validJSON_ThenEmitThreeEvents() {
        Long memberId = 1234L;
        UUID referenceId = UUID.randomUUID();
        String personalNumber = "12121212120";
        String provideJsonResponse = "{ \"json\": true }";

        when(cashbackService.getDefaultId(anyLong())).thenReturn(DEFAULT_CASHBACK);

        fixture
            .given(
                new MemberCreatedEvent(memberId, MemberStatus.INITIATED),
                new MemberStartedOnBoardingEvent(memberId, MemberStatus.ONBOARDING),
                new TrackingIdCreatedEvent(memberId, TRACKING_UUID))
            .when(new ZignSecSignCommand(memberId, referenceId, personalNumber, provideJsonResponse, ZignSecAuthenticationMarket.NORWAY))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                new NewCashbackSelectedEvent(memberId, DEFAULT_CASHBACK.toString()),
                new NorwegianSSNUpdatedEvent(memberId, personalNumber),
                new NorwegianMemberSignedEvent(memberId, personalNumber, provideJsonResponse, referenceId)
            );
    }

    @Test
    public void danishSignCommand_validJSON_ThenEmitThreeEvents() {
        Long memberId = 1234L;
        UUID referenceId = UUID.randomUUID();
        String personalNumber = "1212121212";
        String provideJsonResponse = "{ \"json\": true }";

        when(cashbackService.getDefaultId(anyLong())).thenReturn(DEFAULT_CASHBACK);

        fixture
            .given(
                new MemberCreatedEvent(memberId, MemberStatus.INITIATED),
                new MemberStartedOnBoardingEvent(memberId, MemberStatus.ONBOARDING),
                new TrackingIdCreatedEvent(memberId, TRACKING_UUID))
            .when(new ZignSecSignCommand(memberId, referenceId, personalNumber, provideJsonResponse, ZignSecAuthenticationMarket.DENMARK))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                new NewCashbackSelectedEvent(memberId, DEFAULT_CASHBACK.toString()),
                new DanishSSNUpdatedEvent(memberId, personalNumber),
                new DanishMemberSignedEvent(memberId, personalNumber, provideJsonResponse, referenceId)
            );
    }

    @Test
    public void norwegianSignCommand_invalidJSON_expectException() {
        Long memberId = 1234L;
        UUID referenceId = UUID.randomUUID();
        String personalNumber = "12121212120";
        String provideJsonResponse = "not a valid json";

        when(cashbackService.getDefaultId(anyLong())).thenReturn(DEFAULT_CASHBACK);

        fixture
            .given(
                new MemberCreatedEvent(memberId, MemberStatus.INITIATED),
                new MemberStartedOnBoardingEvent(memberId, MemberStatus.ONBOARDING),
                new TrackingIdCreatedEvent(memberId, TRACKING_UUID))
            .when(new ZignSecSignCommand(memberId, referenceId, personalNumber, provideJsonResponse, ZignSecAuthenticationMarket.NORWAY))
            .expectException(RuntimeException.class);
    }

    @Test
    public void inactivateMemberCommand_givenInitiatedMember_thenEmitsMemberInactivatedEvent() {
        Long memberId = 1234L;

        fixture
            .given(new MemberCreatedEvent(memberId, MemberStatus.INITIATED))
            .when(new InactivateMemberCommand(memberId))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                new MemberInactivatedEvent(memberId));

    }

    @Test
    public void inactivateMemberCommand_givenOnboardingMember_thenEmitsMemberInactivatedEvent() {
        Long memberId = 1234L;

        fixture
            .given(new MemberCreatedEvent(memberId, MemberStatus.INITIATED),
                new MemberStartedOnBoardingEvent(memberId, MemberStatus.ONBOARDING))
            .when(new InactivateMemberCommand(memberId))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                new MemberInactivatedEvent(memberId));

    }

    @Test
    public void pickedLocaleUpdateCommand_givenNotSigned_thenUpdatePickedLocale() {
        Long memberId = 1234L;

        fixture
            .given(new MemberCreatedEvent(memberId, MemberStatus.INITIATED))
            .when(new UpdatePickedLocaleCommand(memberId, PickedLocale.sv_SE))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                new PickedLocaleUpdatedEvent(memberId, PickedLocale.sv_SE)
            );
    }

    @Test
    public void pickedLocaleUpdateCommand_givenSigned_thenExpectUpdatePickedLocaleEvents() {
        Long memberId = 1234L;
        String referenceId = "someReferenceId";
        String personalNumber = "198902171234";

        when(uuidGenerator.generateRandom()).thenReturn(TRACKING_UUID);

        when(cashbackService.getDefaultId(anyLong())).thenReturn(DEFAULT_CASHBACK);

        fixture
            .given(
                new MemberCreatedEvent(memberId, MemberStatus.INITIATED),
                new SSNUpdatedEvent(memberId, personalNumber),
                new NewCashbackSelectedEvent(memberId, DEFAULT_CASHBACK.toString()),
                new MemberSignedEvent(memberId, referenceId, "", "", personalNumber),
                new PickedLocaleUpdatedEvent(memberId, PickedLocale.sv_SE),
                new TrackingIdCreatedEvent(memberId, TRACKING_UUID)
            )
            .when(new UpdatePickedLocaleCommand(memberId, PickedLocale.nb_NO))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                new PickedLocaleUpdatedEvent(memberId, PickedLocale.nb_NO)
            );
    }

    @Test
    public void updateSSNCommand_whenMemberIsCreated_shouldUpdateSsnAndBirthdate() {
        Long memberId = 1234L;
        String personalNumber = "198902171234";

        fixture
            .given(
                new MemberCreatedEvent(memberId, MemberStatus.INITIATED)
            )
            .when(new UpdateSSNCommand(memberId, personalNumber))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                new SSNUpdatedEvent(memberId, personalNumber),
                new BirthDateUpdatedEvent(memberId, LocalDate.of(1989, 2, 17))
            );
    }

    @Test
    public void updateWebOnBoardingInfoCommand_whenMemberIsCreated_shouldUpdateSsnAndBirthdateAndEmail() {
        Long memberId = 1234L;
        String personalNumber = "198902171234";
        String email = "em@i.l";

        fixture
            .given(
                new MemberCreatedEvent(memberId, MemberStatus.INITIATED)
            )
            .when(new UpdateWebOnBoardingInfoCommand(memberId, personalNumber, email))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                new SSNUpdatedEvent(memberId, personalNumber),
                new BirthDateUpdatedEvent(memberId, LocalDate.of(1989, 2, 17)),
                new EmailUpdatedEvent(memberId, email)
            );
    }

    @Test
    public void memberSimpleSignedCommand_whenMemberIsCreated_shouldSignMemberAndUpdateSsn() {
        Long memberId = 1234L;
        String personalNumber = "198902171234";
        UUID refId = UUID.randomUUID();

        fixture
            .given(
                new MemberCreatedEvent(memberId, MemberStatus.INITIATED)
            )
            .when(new MemberSimpleSignedCommand(memberId, personalNumber, refId))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                new MemberSimpleSignedEvent(memberId, personalNumber, refId),
                new SSNUpdatedEvent(memberId, personalNumber)
            );
    }

    @Test
    public void updateBirthDateCommand_whenMemberIsCreated_should() {
        Long memberId = 1234L;
        LocalDate birthDate = LocalDate.of(1978, 2, 3);

        fixture
            .given(
                new MemberCreatedEvent(memberId, MemberStatus.INITIATED)
            )
            .when(new UpdateBirthDateCommand(memberId, birthDate))
            .expectSuccessfulHandlerExecution()
            .expectEvents(
                new BirthDateUpdatedEvent(memberId, birthDate)
            );
    }

    private class AggregateFactoryM<T> extends AbstractAggregateFactory<T> {

        AggregateFactoryM(Class<T> aggregateType) {
            super(aggregateType);
        }

        @Override
        protected T doCreateAggregate(String aggregateIdentifier, DomainEventMessage firstEvent) {
            return (T) new MemberAggregate(bisnodeClient, cashbackService, uuidGenerator, objectMapper);
        }
    }
}
