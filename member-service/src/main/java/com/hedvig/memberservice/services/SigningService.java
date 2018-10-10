package com.hedvig.memberservice.services;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import com.hedvig.external.bankID.bankIdRestTypes.CollectStatus;
import com.hedvig.external.bankID.bankIdRestTypes.OrderResponse;
import com.hedvig.memberservice.enteties.CollectResponse;
import com.hedvig.memberservice.enteties.SignSession;
import com.hedvig.memberservice.enteties.SignSessionRepository;
import com.hedvig.memberservice.enteties.SignStatus;
import com.hedvig.memberservice.externalApi.productsPricing.ProductApi;
import com.hedvig.memberservice.jobs.BankIdCollector;
import com.hedvig.memberservice.query.SignedMemberRepository;
import com.hedvig.memberservice.services.events.SignSessionCompleteEvent;
import com.hedvig.memberservice.services.member.CannotSignInsuranceException;
import com.hedvig.memberservice.services.member.MemberService;
import com.hedvig.memberservice.services.member.dto.MemberSignResponse;
import com.hedvig.memberservice.web.v2.dto.WebsignRequest;
import java.util.Optional;
import javax.transaction.Transactional;
import lombok.val;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class SigningService {

  private static final Logger log = LoggerFactory.getLogger(SigningService.class);

  private final BankIdRestService bankidService;
  private final ProductApi productApi;
  private final SignedMemberRepository signedMemberRepository;
  private final SignSessionRepository signSessionRepository;
  private final Scheduler scheduler;
  private final MemberService memberService;
  private final ApplicationEventPublisher applicationEventPublisher;

  public SigningService(
      BankIdRestService bankidService,
      ProductApi productApi,
      SignedMemberRepository signedMemberRepository,
      SignSessionRepository signSessionRepository,
      Scheduler scheduler,
      MemberService memberService,
      ApplicationEventPublisher applicationEventPublisher) {
    this.bankidService = bankidService;
    this.productApi = productApi;
    this.signedMemberRepository = signedMemberRepository;
    this.signSessionRepository = signSessionRepository;
    this.scheduler = scheduler;
    this.memberService = memberService;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Transactional
  public MemberSignResponse startWebSign(final long memberId, final WebsignRequest request) {

    val existing = signedMemberRepository.findBySsn(request.getSsn());

    if (existing.isPresent()) {
      throw new MemberHasExistingInsuranceException();
    }

    if (productApi.hasProductToSign(memberId)) {
      val result = bankidService.startSign(request.getSsn(), "", request.getIpAddress());

      val session = new SignSession(memberId);
      session.setAutoStartToken(result.getAutoStartToken());
      session.setOrderReference(result.getOrderRef());
      session.setStatus(SignStatus.IN_PROGRESS);

      signSessionRepository.save(session);

      scheduleCollectJob(result);

      return new MemberSignResponse(session.getSessionId(), SignStatus.IN_PROGRESS, result);
    } else {
      throw new CannotSignInsuranceException();
    }
  }

  @Transactional
  public void scheduleCollectJob(OrderResponse result) {
    try {
      val jobName = result.getOrderRef();
      val jobDetail = newJob().withIdentity(jobName, "bankid.collect").ofType(
          BankIdCollector.class).build();

      val trigger = newTrigger()
          .forJob(jobName, "bankid.collect")
          .withSchedule(simpleSchedule().withIntervalInSeconds(1).withRepeatCount(900).withMisfireHandlingInstructionNowWithRemainingCount())
          .build();

      scheduler.scheduleJob(jobDetail,
          trigger);

    } catch (SchedulerException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   *
   * @param orderReference
   * @return true if BankID needs to be collected again, otherwise false
   */
  @Transactional
  public boolean collectBankId(@NonNull final String orderReference) {

    val session = signSessionRepository.findByOrderReference(orderReference);

    return session
        .map(
            s -> {
              if (s.getStatus() == SignStatus.IN_PROGRESS) {
                val response = bankidService.collect(orderReference);

                final CollectResponse collectResponse = new CollectResponse();
                collectResponse.setStatus(response.getStatus());
                collectResponse.setHintCode(response.getHintCode());
                s.setCollectResponse(collectResponse);

                if(response.getStatus() == CollectStatus.complete){
                  memberService.bankIdSignComplete(s.getMemberId(), response);
                  s.setStatus(SignStatus.COMPLETE);
                }else if(response.getStatus() == CollectStatus.failed){
                  s.setStatus(SignStatus.FAILED);
                }

                signSessionRepository.save(s);

                return response.getStatus() == CollectStatus.pending;
              }

              return false;
            })
        .orElseGet(
            () -> {
              log.error("Could not find SignSession with orderReference: ", orderReference);
              return false;
            });
  }

  public Optional<SignSession> getSignStatus(@NonNull final String orderRef) {
    return signSessionRepository.findByOrderReference(orderRef);
  }

  @Transactional
  public void productSignConfirmed(String id) {
    val session = signSessionRepository.findByOrderReference(id);

    session.ifPresent(s -> {
      s.setStatus(SignStatus.COMPLETE);
      signSessionRepository.save(s);
      applicationEventPublisher.publishEvent(new SignSessionCompleteEvent(s.getMemberId()));
    });
  }

  private String createUserSignText(boolean mandateSing) {
    String signText;
    if (mandateSing) {
      signText =
          "Jag har tagit del av förköpsinformation och villkor och bekräftar genom att signera att jag vill byta till Hedvig när min gamla försäkring går ut. Jag ger också  Hedvig fullmakt att byta försäkringen åt mig.";
    } else {
      signText =
          "Jag har tagit del av förköpsinformation och villkor och bekräftar genom att signera att jag skaffar en försäkring hos Hedvig.";
    }
    return signText;
  }
}