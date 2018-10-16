package com.hedvig.memberservice.services;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import com.hedvig.external.bankID.bankIdRestTypes.BankIdRestError;
import com.hedvig.external.bankID.bankIdRestTypes.CollectStatus;
import com.hedvig.external.bankID.bankIdRestTypes.OrderResponse;
import com.hedvig.memberservice.commands.UpdateWebOnBoardingInfoCommand;
import com.hedvig.memberservice.entities.CollectResponse;
import com.hedvig.memberservice.entities.SignSession;
import com.hedvig.memberservice.entities.SignSessionRepository;
import com.hedvig.memberservice.entities.SignStatus;
import com.hedvig.memberservice.externalApi.botService.BotService;
import com.hedvig.memberservice.externalApi.botService.dto.UpdateUserContextDTO;
import com.hedvig.memberservice.externalApi.productsPricing.ProductApi;
import com.hedvig.memberservice.jobs.BankIdCollector;
import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.query.MemberRepository;
import com.hedvig.memberservice.query.SignedMemberRepository;
import com.hedvig.memberservice.services.member.CannotSignInsuranceException;
import com.hedvig.memberservice.services.member.MemberService;
import com.hedvig.memberservice.services.member.dto.MemberSignResponse;
import com.hedvig.memberservice.web.v2.dto.WebsignRequest;
import java.util.Optional;
import javax.transaction.Transactional;
import lombok.val;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
  private final MemberRepository memberRepository;
  private final BotService botService;
  private final CommandGateway commandGateway;

  private final String switcherMessage;
  private final String nonSwitcherMessage;

  public SigningService(
      BankIdRestService bankidService,
      ProductApi productApi,
      SignedMemberRepository signedMemberRepository,
      SignSessionRepository signSessionRepository,
      Scheduler scheduler,
      MemberService memberService,
      MemberRepository memberRepository,
      BotService botService,
      CommandGateway commandGateway,
      @Value("${hedvig.bankid.signmessage.switcher}") String switcherMessage,
      @Value("${hedvig.bankid.signmessage.nonSwitcher}") String nonSwitcherMessage) {
    this.bankidService = bankidService;
    this.productApi = productApi;
    this.signedMemberRepository = signedMemberRepository;
    this.signSessionRepository = signSessionRepository;
    this.scheduler = scheduler;
    this.memberService = memberService;
    this.memberRepository = memberRepository;
    this.botService = botService;
    this.commandGateway = commandGateway;
    this.switcherMessage = switcherMessage;
    this.nonSwitcherMessage = nonSwitcherMessage;
  }

  @Transactional
  public MemberSignResponse startWebSign(final long memberId, final WebsignRequest request) {

    val existing = signedMemberRepository.findBySsn(request.getSsn());

    if (existing.isPresent()) {
      throw new MemberHasExistingInsuranceException();
    }

    val productStatus = productApi.hasProductToSign(memberId);
    if (productStatus.isEligibleToSign()) {

      val session = signSessionRepository.findByMemberId(memberId).orElseGet(() -> new SignSession(memberId));

      if (session.canReuseBankIdSession() == false) {

        val result =
            bankidService.startSign(
                request.getSsn(),
                createUserSignText(productStatus.isSwitching()),
                request.getIpAddress());

        session.newOrderStarted(result);

        signSessionRepository.save(session);
        scheduleCollectJob(result);

        return new MemberSignResponse(session.getSessionId(), SignStatus.IN_PROGRESS, result);
      }

      UpdateWebOnBoardingInfoCommand cmd = new UpdateWebOnBoardingInfoCommand(memberId,
          request.getSsn(), request.getEmail());
      commandGateway.sendAndWait(cmd);

      return new MemberSignResponse(session.getSessionId(), SignStatus.IN_PROGRESS,
          session.getOrderResponse());

    } else {
      throw new CannotSignInsuranceException();
    }
  }

  @Transactional
  public void scheduleCollectJob(OrderResponse result) {
    try {
      val jobName = result.getOrderRef();
      val jobDetail = newJob()
          .withIdentity(jobName, "bankid.collect")
          .ofType(BankIdCollector.class)
          .build();

      val trigger = newTrigger()
          .forJob(jobName, "bankid.collect")
          .withSchedule(simpleSchedule().withIntervalInSeconds(1).withRepeatCount(900)
              .withMisfireHandlingInstructionNowWithRemainingCount())
          .build();

      scheduler.scheduleJob(jobDetail,
          trigger);

    } catch (SchedulerException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * @param orderReference order reference from bankId
   * @return true if BankID needs to be collected again, otherwise false
   */
  @Transactional
  public boolean collectBankId(@NonNull final String orderReference) {

    val session = signSessionRepository.findByOrderReference(orderReference);

    return session
        .map(
            s -> {
              if (s.getStatus() == SignStatus.IN_PROGRESS) {
                try {
                  val response = bankidService.collect(orderReference);

                  final CollectResponse collectResponse = new CollectResponse();
                  collectResponse.setStatus(response.getStatus());
                  collectResponse.setHintCode(response.getHintCode());
                  s.newCollectResponse(collectResponse);

                  if (response.getStatus() == CollectStatus.complete) {
                    memberService.bankIdSignComplete(s.getMemberId(), response);
                    s.setStatus(SignStatus.COMPLETED);
                  } else if (response.getStatus() == CollectStatus.failed) {
                    s.setStatus(SignStatus.FAILED);
                  }

                  signSessionRepository.save(s);

                  return response.getStatus() == CollectStatus.pending;
                } catch (BankIdRestError e) {
                  s.setStatus(SignStatus.FAILED);
                  signSessionRepository.save(s);
                }
              }

              return false;
            })
        .orElseGet(
            () -> {
              log.error("Could not find SignSession with orderReference: ", orderReference);
              return false;
            });
  }

  public Optional<SignSession> getSignStatus(@NonNull final long orderRef) {
    return signSessionRepository.findByMemberId(orderRef);
  }

  @Transactional
  public void productSignConfirmed(String id) {
    val session = signSessionRepository.findByOrderReference(id);

    session.ifPresent(s -> {
      s.setStatus(SignStatus.COMPLETED);
      signSessionRepository.save(s);

      MemberEntity member = memberRepository.getOne(s.getMemberId());

      UpdateUserContextDTO userContext = new UpdateUserContextDTO(String.valueOf(
          s.getMemberId()),
          member.getSsn(),
          member.getFirstName(),
          member.getLastName(),
          member.getPhoneNumber(),
          member.getEmail(),
          member.getStreet(),
          member.getCity(),
          member.getZipCode(),
          true);

      botService.initBotServiceSessionWebOnBoarding(s.getMemberId(), userContext);
    });
  }

  private String createUserSignText(boolean isSwitching) {
    String signText;
    if (isSwitching) {
      signText = switcherMessage;
    } else {
      signText = nonSwitcherMessage;
    }
    return signText;
  }
}