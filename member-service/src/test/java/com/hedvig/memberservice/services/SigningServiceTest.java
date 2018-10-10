package com.hedvig.memberservice.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.hedvig.external.bankID.bankIdRestTypes.BankIdRestError;
import com.hedvig.external.bankID.bankIdRestTypes.Collect.Cert;
import com.hedvig.external.bankID.bankIdRestTypes.Collect.Device;
import com.hedvig.external.bankID.bankIdRestTypes.Collect.User;
import com.hedvig.external.bankID.bankIdRestTypes.CollectResponse;
import com.hedvig.external.bankID.bankIdRestTypes.CollectStatus;
import com.hedvig.external.bankID.bankIdRestTypes.CompletionData;
import com.hedvig.external.bankID.bankIdRestTypes.OrderResponse;
import com.hedvig.memberservice.enteties.SignSession;
import com.hedvig.memberservice.enteties.SignSessionRepository;
import com.hedvig.memberservice.enteties.SignStatus;
import com.hedvig.memberservice.externalApi.productsPricing.ProductApi;
import com.hedvig.memberservice.query.SignedMemberEntity;
import com.hedvig.memberservice.query.SignedMemberRepository;
import com.hedvig.memberservice.services.events.SignSessionCompleteEvent;
import com.hedvig.memberservice.services.member.CannotSignInsuranceException;
import com.hedvig.memberservice.services.member.MemberService;
import com.hedvig.memberservice.web.v2.dto.WebsignRequest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;
import lombok.val;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationEventPublisher;

@RunWith(MockitoJUnitRunner.class)
public class SigningServiceTest {

  private static final String ORDER_REFERENCE = "orderReference";
  private static final long MEMBER_ID = 1337L;
  private static final String SSN = "191212121212";
  private static final String EMAIL = "test@test.com";
  private static final String AUTOSTART_TOKEN = "autostartToken";

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @Mock
  ProductApi productApi;

  @Mock
  BankIdRestService bankIdRestService;

  @Mock
  SignedMemberRepository signedMemberRepository;

  @Mock
  SignSessionRepository signSessionRepository;

  @Mock
  Scheduler scheduler;

  @Mock
  MemberService memberService;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  ArgumentCaptor<JobDetail> jobDetailArgumentCaptor = ArgumentCaptor.forClass(JobDetail.class);


  SigningService sut;
  ;


  @Before
  public void setup(){
    given(signedMemberRepository.findBySsn(any())).willReturn(Optional.empty());
    sut = new SigningService(bankIdRestService, productApi, signedMemberRepository,
        signSessionRepository, scheduler, memberService, applicationEventPublisher);
  }



  @Test
  public void startWebSign_givenMemberWithOkProduct_thenReturnOrderRefAndAutoStartToken(){

    given(productApi.hasProductToSign(MEMBER_ID)).willReturn(true);
    given(bankIdRestService.startSign(matches(SSN), anyString(), anyString()))
        .willReturn(new OrderResponse(ORDER_REFERENCE, "autostartToken"));

    val result = sut.startWebSign(MEMBER_ID, new WebsignRequest(EMAIL, SSN,"127.0.0.1"));

    assertThat(result.getBankIdOrderResponse()).hasFieldOrProperty("orderRef");
    assertThat(result.getBankIdOrderResponse()).hasFieldOrProperty("autoStartToken");

  }

  @Test
  public void startWebSign_givenMemberWithOkProduct_thenSchedulesCollectJob()
      throws SchedulerException {

    given(productApi.hasProductToSign(MEMBER_ID)).willReturn(true);
    given(bankIdRestService.startSign(matches(SSN), anyString(), anyString()))
        .willReturn(new OrderResponse(
            ORDER_REFERENCE, AUTOSTART_TOKEN));

    given(scheduler.scheduleJob(jobDetailArgumentCaptor.capture(), any())).willReturn(Date.from(
        Instant.now()));

    sut.startWebSign(MEMBER_ID, new WebsignRequest(EMAIL, SSN, "127.0.0.1"));

    then(scheduler).should(times(1)).scheduleJob(any(), any());

    assertThat(jobDetailArgumentCaptor.getValue().getKey().getName()).isEqualTo(ORDER_REFERENCE);
  }

  @Test
  public void startWebSign_givenMemberWithoutOkProduct_thenThrowException(){

    given(productApi.hasProductToSign(MEMBER_ID)).willReturn(false);

    thrown.expect(CannotSignInsuranceException.class);
    sut.startWebSign(MEMBER_ID, new WebsignRequest(EMAIL, SSN, "127.0.0.1"));
  }

  @Test
  public void startWebSign_givenMemberInSignedMemberEntity_thenThrowException(){

    val memberId = MEMBER_ID;


    val memberEntity = new SignedMemberEntity();
    memberEntity.setId(memberId);
    memberEntity.setSsn(SSN);
    given(signedMemberRepository.findBySsn(SSN)).willReturn(Optional.of(memberEntity));


    thrown.expect(MemberHasExistingInsuranceException.class);
    sut.startWebSign(memberId, new WebsignRequest(EMAIL, SSN, "127.0.0.1"));
  }

  @Test
  public void startWebSign_givenBankidThrowsError_thenThrowException(){

    given(productApi.hasProductToSign(MEMBER_ID)).willReturn(true);
    given(bankIdRestService.startSign(any(), any(), anyString()))
        .willThrow(BankIdRestError.class);


    thrown.expect(BankIdRestError.class);
    sut.startWebSign(MEMBER_ID, new WebsignRequest(EMAIL, SSN, "127.0.0.1"));

  }

  @Test
  public void collectBankId_givenSessionInProgress_thenReturnTrue() {
    SignSession session = makeSignSession(SignStatus.IN_PROGRESS);

    given(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
        .willReturn(Optional.of(session));

    val response = makeCollectResponse(CollectStatus.pending);
    given(bankIdRestService.collect(ORDER_REFERENCE)).willReturn(response);

    val acctual = sut.collectBankId(ORDER_REFERENCE);

    assertThat(acctual).isEqualTo(true);
  }

  @Test
  public void collectBankId_givenSessionThatIsComplete_thenReturnFalse() {
    SignSession session = makeSignSession(SignStatus.COMPLETE);

    given(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
        .willReturn(Optional.of(session));

    val status = sut.collectBankId(ORDER_REFERENCE);

    assertThat(status).isEqualTo(false);
    then(bankIdRestService).shouldHaveZeroInteractions();

  }

  @Test
  public void collectBankId_givenSessionThatIsFailed_thenReturnFalse() {
    SignSession session = makeSignSession(SignStatus.FAILED);

    given(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
        .willReturn(Optional.of(session));

    val status = sut.collectBankId(ORDER_REFERENCE);

    assertThat(status).isEqualTo(false);
    then(bankIdRestService).shouldHaveZeroInteractions();

  }

  @Test
  public void collectBankId_givenNoMatchingOrderReference_thenReturnFalse() {

    given(signSessionRepository.findByOrderReference(ORDER_REFERENCE)).willReturn(Optional.empty());

    val status = sut.collectBankId(ORDER_REFERENCE);

    assertThat(status).isEqualTo(false);
    then(bankIdRestService)
        .shouldHaveZeroInteractions();

  }

  @Test
  public void collectBankId_givenBankIdReturnPending_thenReturnTrue() {

    val session = makeSignSession(SignStatus.IN_PROGRESS);
    given(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
        .willReturn(Optional.of(session));

    val response = makeCollectResponse(CollectStatus.pending);
    given(bankIdRestService.collect(eq(ORDER_REFERENCE))).willReturn(response);

    val status = sut.collectBankId(ORDER_REFERENCE);

    assertThat(status).isEqualTo(true);
    assertThat(session.getCollectResponse())
        .hasFieldOrPropertyWithValue("status", CollectStatus.pending);
    then(signSessionRepository).should(times(1)).save(eq(session));

  }

  @Test
  public void collectBankId_givenBankIdReturnComplete_thenReturnFalse() {

    val session = makeSignSession(SignStatus.IN_PROGRESS);
    given(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
        .willReturn(Optional.of(session));

    val response = makeCollectResponse(CollectStatus.complete);
    given(bankIdRestService.collect(eq(ORDER_REFERENCE))).willReturn(response);

    val status = sut.collectBankId(ORDER_REFERENCE);

    assertThat(status).isEqualTo(false);
    assertThat(session.getCollectResponse())
        .hasFieldOrPropertyWithValue("status", CollectStatus.complete);
    then(signSessionRepository).should(times(1)).save(eq(session));

  }

  @Test
  public void collectBankId_givenBankIdReturnComplete_thenSendBankIdCompleteToMemberService() {

    val session = makeSignSession(SignStatus.IN_PROGRESS);
    given(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
        .willReturn(Optional.of(session));

    val response = makeCollectResponse(CollectStatus.complete);
    given(bankIdRestService.collect(eq(ORDER_REFERENCE))).willReturn(response);

    sut.collectBankId(ORDER_REFERENCE);

    then(memberService).should(times(1)).bankIdSignComplete(eq(MEMBER_ID), eq(response));

  }

  @Test
  public void collectBankId_givenBankIdReturnComplete_thenSetsSignStatusToComplete() {

    val session = makeSignSession(SignStatus.IN_PROGRESS);
    given(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
        .willReturn(Optional.of(session));

    val response = makeCollectResponse(CollectStatus.complete);
    given(bankIdRestService.collect(eq(ORDER_REFERENCE))).willReturn(response);

    sut.collectBankId(ORDER_REFERENCE);

    assertThat(session.getStatus()).isEqualTo(SignStatus.COMPLETE);

  }

  public CollectResponse makeCollectResponse(CollectStatus collectStatus) {
    val userData = new User(SSN, "Tolvan Tolvansson", "Tolvan", "Tolvansson");
    val device = new Device("127.0.0.1");
    val cert = new Cert(
        LocalDateTime.parse("2018-09-01T00:00:00").toInstant(
            ZoneOffset.UTC).toEpochMilli(),
        LocalDateTime.parse("2020-09-01T00:00:00").toInstant(
            ZoneOffset.UTC).toEpochMilli());
    val completionData = new CompletionData(userData, device, cert, "", "");

    return new CollectResponse(ORDER_REFERENCE, collectStatus,
        collectStatus == CollectStatus.complete ? null : "someHint",
        collectStatus == CollectStatus.complete ? completionData : null);
  }

  @Test
  public void collectBankId_givenBankIdReturnFailed_thenSetsSignStatusToFailed() {

    val session = makeSignSession(SignStatus.IN_PROGRESS);
    given(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
        .willReturn(Optional.of(session));

    val response = makeCollectResponse(CollectStatus.failed);
    given(bankIdRestService.collect(eq(ORDER_REFERENCE))).willReturn(response);

    sut.collectBankId(ORDER_REFERENCE);

    assertThat(session.getStatus()).isEqualTo(SignStatus.FAILED);

  }

  @Test
  public void getSignStatus_givenNoMatchingSignStatus_thenReturnEmpty() {
    given(signSessionRepository.findByOrderReference(ORDER_REFERENCE)).willReturn(Optional.empty());

    val status = sut.getSignStatus(ORDER_REFERENCE);

    assertThat(status).isEmpty();
  }

  @Test
  public void getSignStatus_givenMatchingSignStatus_thenReturnSignStatus() {
    val session = makeSignSession(SignStatus.IN_PROGRESS);
    given(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
        .willReturn(Optional.of(session));

    val status = sut.getSignStatus(ORDER_REFERENCE);

    assertThat(status).get().isEqualTo(session);
  }

  @Test
  public void productSignConfirmed_givenSignSession_thenSetsStatusToComplete() {
    val session = makeSignSession(SignStatus.IN_PROGRESS);

    given(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
        .willReturn(Optional.of(session));

    sut.productSignConfirmed(ORDER_REFERENCE);

    assertThat(session.getStatus()).isEqualTo(SignStatus.COMPLETE);
    then(signSessionRepository).should(times(1)).save(session);

  }

  @Test
  public void productSignConfirmed_givenSignSession_thenSendsEventOnRabbitMq() {
    val session = makeSignSession(SignStatus.IN_PROGRESS);

    given(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
        .willReturn(Optional.of(session));

    sut.productSignConfirmed(ORDER_REFERENCE);

    then(applicationEventPublisher).should(times(1)).publishEvent(eq(new SignSessionCompleteEvent(MEMBER_ID)));

  }

  @Test
  public void productSignConfirmed_givenNoSignSession_thenDoesNothing() {

    given(signSessionRepository.findByOrderReference(ORDER_REFERENCE)).willReturn(Optional.empty());

    sut.productSignConfirmed(ORDER_REFERENCE);

    then(signSessionRepository).should(times(0)).save(any());

  }

  private static SignSession makeSignSession(SignStatus inProgress) {
    val session = new SignSession(MEMBER_ID);
    session.setOrderReference(ORDER_REFERENCE);
    session.setStatus(inProgress);
    return session;
  }

}