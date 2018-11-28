package com.hedvig.memberservice.web;

import com.hedvig.memberservice.aggregates.FraudulentStatus;
import com.hedvig.memberservice.aggregates.MemberStatus;
import com.hedvig.memberservice.commands.*;
import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.query.MemberRepository;
import com.hedvig.memberservice.services.member.MemberQueryService;
import com.hedvig.memberservice.services.trace.TraceMemberService;
import com.hedvig.memberservice.web.dto.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.val;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/i/member", "/_/member"})
public class InternalMembersController {

  private final Logger log = LoggerFactory.getLogger(InternalMembersController.class);
  private final CommandGateway commandBus;
  private final MemberRepository memberRepository;
  private final MemberQueryService memberQueryService;
  private final TraceMemberService traceMemberService;

  public InternalMembersController(CommandGateway commandBus, MemberRepository memberRepository,
      MemberQueryService memberQueryService, TraceMemberService traceMemberService) {
    this.commandBus = commandBus;
    this.memberRepository = memberRepository;
    this.memberQueryService = memberQueryService;
    this.traceMemberService = traceMemberService;
  }

  @GetMapping("/{memberId}")
  public ResponseEntity<InternalMember> index(@PathVariable Long memberId) {

    Optional<MemberEntity> member = memberRepository.findById(memberId);
    if (member.isPresent()) {

      InternalMember internalMember = InternalMember.fromEntity(member.get());
      internalMember.getTraceMemberInfo().addAll(traceMemberService.getTracesByMemberId(memberId));
      return ResponseEntity.ok(internalMember);
    }

    return ResponseEntity.notFound().build();
  }

  @RequestMapping(value = "/{memberId}/finalizeOnboarding", method = RequestMethod.POST)
  public ResponseEntity<?> finalizeOnboarding(
      @PathVariable Long memberId, @RequestBody UpdateContactInformationRequest body, @RequestHeader("Authorization") String token) {

    MemberUpdateContactInformationCommand finalizeOnBoardingCommand =
        new MemberUpdateContactInformationCommand(memberId, body, token);

    commandBus.sendAndWait(finalizeOnBoardingCommand);

    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/{memberId}/startOnboardingWithSSN", method = RequestMethod.POST)
  public ResponseEntity<?> startOnboardingWithSSN(
      @PathVariable Long memberId, @RequestBody StartOnboardingWithSSNRequest request) {

    try {
      commandBus.sendAndWait(new StartOnboardingWithSSNCommand(memberId, request));
    } catch (RuntimeException ex) {
      return ResponseEntity.badRequest().body("{\"message\":\"" + ex.getMessage() + "\"");
    }

    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/{memberId}/updateEmail", method = RequestMethod.POST)
  public ResponseEntity<?> updateEmail(
      @PathVariable Long memberId, @RequestBody UpdateEmailRequest request, @RequestHeader("Authorization") String token) {

    commandBus.sendAndWait(new UpdateEmailCommand(memberId, request.getEmail(), token));

    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/{memberId}/updatePhoneNumber")
  public ResponseEntity<?> updatePhoneNumber(@PathVariable Long memberId,
      @RequestBody UpdatePhoneNumberRequest request, @RequestHeader("Authorization") String token) {
    commandBus.sendAndWait(new UpdatePhoneNumberCommand(memberId, request.getPhoneNumber(), token));
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/search", method = RequestMethod.GET)
  public List<InternalMember> searchMembers(
      @RequestParam(name = "status", defaultValue = "", required = false) String status,
      @RequestParam(name = "query", defaultValue = "", required = false) String query) {

    query = query.trim();

    InternalMemberSearchRequestDTO req = new InternalMemberSearchRequestDTO();
    req.setQuery(query);
    try {
      if (StringUtils.hasText(status)) {
        req.setStatus(MemberStatus.valueOf(status.trim()));
      }
    } catch (Exception e) {
      //for backward compatibility
      return Collections.emptyList();
    }

    return memberQueryService.search(req).getMembers();
  }

  @RequestMapping(value = "/searchPaged", method = RequestMethod.GET)
  public InternalMemberSearchResultDTO searchMembersPaged(InternalMemberSearchRequestDTO req) {
    return memberQueryService.search(req);
  }

  @RequestMapping(value = "/{memberId}/cancelMembership", method = RequestMethod.POST)
  public ResponseEntity<?> memberCancellation(
      @PathVariable Long memberId, @RequestBody MemberCancelInsurance body) {
    log.info("Dispatching MemberCancellation for member ({})", memberId);
    commandBus.sendAndWait(new MemberCancelInsuranceCommand(memberId, body.getCancellationDate()));
    return ResponseEntity.accepted().build();
  }

  @RequestMapping(value = "/{memberId}/memberCancelInsurance", method = RequestMethod.POST)
  public ResponseEntity<?> insuranceCancellation(
    @PathVariable Long memberId, @RequestBody InsuranceCancellationDTO request) {
    log.info("Dispatching Insurance Cancelation for member ({})", memberId);
    commandBus.sendAndWait(
      new InsurnaceCancellationCommand(
        memberId, request.getInsuranceId(), request.getCancellationDate()));
    return ResponseEntity.accepted().build();
  }

  @RequestMapping(value = "/{memberId}/setFraudulentStatus", method = RequestMethod.POST)
  public ResponseEntity<?> fraudulentStatus(
    @PathVariable Long memberId, @RequestBody MemberFraudulentStatusDTO request, @RequestHeader("Authorization") String token) {
    log.info("Change Fraudulent status for member ({}) with {} and {}", memberId, request.getFraudulentStatus(), request.getFraudulentStatusDescription());
    commandBus.sendAndWait(
      new SetFraudulentStatusCommand(memberId, request.getFraudulentStatus(), request.getFraudulentStatusDescription(), token));
    return ResponseEntity.accepted().build();
  }

  @PostMapping("{memberId}/edit")
  public void editMember(
      @PathVariable("memberId") String memberId,
      @RequestBody InternalMember dto,
      @RequestHeader("Authorization") String token) {

    Optional<MemberEntity> member = memberRepository.findById(Long.parseLong(memberId));

    if (member.isPresent() && !InternalMember.fromEntity(member.get()).equals(dto)) {
      commandBus.sendAndWait(new EditMemberInformationCommand(memberId, dto, token));
    }
  }

  @PostMapping("/many")
  public ResponseEntity<List<InternalMember>> getMembers(@RequestBody ChargeMembersDTO dto) {
    val members =
        memberRepository
            .findAllByIdIn(
                dto.getMemberIds().stream().map(Long::parseLong).collect(Collectors.toList()))
            .stream()
            .map(m -> InternalMember.fromEntity(m))
            .collect(Collectors.toList());

    if (dto.getMemberIds().size() != members.size()) {
      log.error(
          "Length mismatch of supplied members and found members: wanted {}, found {}",
          dto.getMemberIds().size(),
          members.size());
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(members);
  }


}
