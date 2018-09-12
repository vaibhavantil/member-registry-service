package com.hedvig.memberservice;

import static org.mockito.Mockito.when;

import com.hedvig.common.UUIDGenerator;
import com.hedvig.external.bisnodeBCI.BisnodeClient;
import com.hedvig.memberservice.aggregates.MemberAggregate;
import com.hedvig.memberservice.aggregates.MemberStatus;
import com.hedvig.memberservice.commands.AuthenticationAttemptCommand;
import com.hedvig.memberservice.commands.BankIdAuthenticationStatus;
import com.hedvig.memberservice.commands.BankIdSignCommand;
import com.hedvig.memberservice.commands.InactivateMemberCommand;
import com.hedvig.memberservice.commands.MemberUpdateContactInformationCommand;
import com.hedvig.memberservice.commands.SelectNewCashbackCommand;
import com.hedvig.memberservice.commands.StartOnboardingWithSSNCommand;
import com.hedvig.memberservice.events.EmailUpdatedEvent;
import com.hedvig.memberservice.events.LivingAddressUpdatedEvent;
import com.hedvig.memberservice.events.MemberAuthenticatedEvent;
import com.hedvig.memberservice.events.MemberCreatedEvent;
import com.hedvig.memberservice.events.MemberInactivatedEvent;
import com.hedvig.memberservice.events.MemberSignedEvent;
import com.hedvig.memberservice.events.MemberStartedOnBoardingEvent;
import com.hedvig.memberservice.events.NameUpdatedEvent;
import com.hedvig.memberservice.events.NewCashbackSelectedEvent;
import com.hedvig.memberservice.events.OnboardingStartedWithSSNEvent;
import com.hedvig.memberservice.events.SSNUpdatedEvent;
import com.hedvig.memberservice.events.TrackingIdCreatedEvent;
import com.hedvig.memberservice.services.CashbackService;
import com.hedvig.memberservice.web.dto.Address;
import com.hedvig.memberservice.web.dto.StartOnboardingWithSSNRequest;
import com.hedvig.memberservice.web.dto.UpdateContactInformationRequest;
import java.util.UUID;
import lombok.val;
import org.axonframework.eventsourcing.AbstractAggregateFactory;
import org.axonframework.eventsourcing.DomainEventMessage;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientException;

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
  @MockBean private BisnodeClient bisnodeClient;
  @MockBean private CashbackService cashbackService;
  @MockBean private UUIDGenerator uuidGenerator;

  @Before
  public void setUp() {
    fixture = new AggregateTestFixture<>(MemberAggregate.class);
    fixture.registerAggregateFactory(new AggregateFactoryM<>(MemberAggregate.class));
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
            new NameUpdatedEvent(memberId, "Tolvan", "Tolvansson"),
            new MemberStartedOnBoardingEvent(memberId, MemberStatus.ONBOARDING),
            new MemberAuthenticatedEvent(memberId, referenceTokenValue));
  }

  @Test
  public void authenticationAttemptedCommand_givenSignedMember_OnlyEmitsMemberAuthenticatedEvent() {
    Long memberId = 1234L;
    String referenceId = "someReferenceId";

    when(uuidGenerator.generateRandom()).thenReturn(TRACKING_UUID);

    when(cashbackService.getDefaultId()).thenReturn(DEFAULT_CASHBACK);

    val bankIdAuthStatus =
        makeBankIdAuthenticationStatus(
            TOLVANSSON_SSN, referenceId, TOLVANSSON_FIRST_NAME, TOLVANSSON_LAST_NAME);

    fixture
        .given(
            new MemberCreatedEvent(memberId, MemberStatus.INITIATED),
            new NewCashbackSelectedEvent(memberId, DEFAULT_CASHBACK.toString()),
            new MemberSignedEvent(memberId, referenceId, "", ""),
            new TrackingIdCreatedEvent(memberId, TRACKING_UUID))
        .when(new AuthenticationAttemptCommand(memberId, bankIdAuthStatus))
        .expectSuccessfulHandlerExecution()
        .expectEvents(new MemberAuthenticatedEvent(memberId, referenceId));
  }

  private BankIdAuthenticationStatus makeBankIdAuthenticationStatus(
      String ssn, String referenceTokenValue, String firstName, String lastName) {
    BankIdAuthenticationStatus authStatus = new BankIdAuthenticationStatus();
    authStatus.setSSN(ssn);
    authStatus.setReferenceToken(referenceTokenValue);
    authStatus.setSurname(lastName);
    authStatus.setGivenName(firstName);
    return authStatus;
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

    when(uuidGenerator.generateRandom()).thenReturn(TRACKING_UUID);

    when(cashbackService.getDefaultId()).thenReturn(DEFAULT_CASHBACK);

    fixture
        .given(new MemberCreatedEvent(memberId, MemberStatus.INITIATED))
        .when(new BankIdSignCommand(memberId, referenceId, "", ""))
        .expectSuccessfulHandlerExecution()
        .expectEvents(
            new NewCashbackSelectedEvent(memberId, DEFAULT_CASHBACK.toString()),
            new MemberSignedEvent(memberId, referenceId, "", ""),
            new TrackingIdCreatedEvent(memberId, TRACKING_UUID));
  }


  @Test
  public void bankIdSignCommand_givenMemberWhoHasAuthenticated_ThenDoNotEmitTrackingIdEvent() {
    Long memberId = 1234L;
    String referenceId = "someReferenceId";

    when(uuidGenerator.generateRandom()).thenReturn(TRACKING_UUID);

    when(cashbackService.getDefaultId()).thenReturn(DEFAULT_CASHBACK);

    fixture
        .given(
            new MemberCreatedEvent(memberId, MemberStatus.INITIATED),
            new MemberAuthenticatedEvent(memberId, referenceId),
            new MemberStartedOnBoardingEvent(memberId, MemberStatus.ONBOARDING),
            new TrackingIdCreatedEvent(memberId, TRACKING_UUID))
        .when(new BankIdSignCommand(memberId, referenceId, "", ""))
        .expectSuccessfulHandlerExecution()
        .expectEvents(
            new NewCashbackSelectedEvent(memberId, DEFAULT_CASHBACK.toString()),
            new MemberSignedEvent(memberId, referenceId, "", ""));
  }

  @Test
  public void inactivateMemberCommand_givenInitiatedMember_thenEmitsMemberInactivatedEvent(){
    Long memberId = 1234L;

    fixture
        .given(new MemberCreatedEvent(memberId, MemberStatus.INITIATED))
        .when(new InactivateMemberCommand(memberId))
        .expectSuccessfulHandlerExecution()
        .expectEvents(
            new MemberInactivatedEvent(memberId));

  }

  @Test
  public void inactivateMemberCommand_givenOnboardingMember_thenEmitsMemberInactivatedEvent(){
    Long memberId = 1234L;

    fixture
        .given(new MemberCreatedEvent(memberId, MemberStatus.INITIATED),
            new MemberStartedOnBoardingEvent(memberId, MemberStatus.ONBOARDING))
        .when(new InactivateMemberCommand(memberId))
        .expectSuccessfulHandlerExecution()
        .expectEvents(
            new MemberInactivatedEvent(memberId));

  }

  private class AggregateFactoryM<T> extends AbstractAggregateFactory<T> {

    AggregateFactoryM(Class<T> aggregateType) {
      super(aggregateType);
    }

    @Override
    protected T doCreateAggregate(String aggregateIdentifier, DomainEventMessage firstEvent) {
      return (T) new MemberAggregate(bisnodeClient, cashbackService, uuidGenerator);
    }
  }
}
