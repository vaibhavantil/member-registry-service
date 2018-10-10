package com.hedvig.memberservice.web;

import com.hedvig.memberservice.commands.EditMemberInformationCommand;
import com.hedvig.memberservice.commands.InsurnaceCancellationCommand;
import com.hedvig.memberservice.commands.MemberCancelInsuranceCommand;
import com.hedvig.memberservice.commands.MemberUpdateContactInformationCommand;
import com.hedvig.memberservice.commands.StartOnboardingWithSSNCommand;
import com.hedvig.memberservice.commands.UpdateEmailCommand;
import com.hedvig.memberservice.commands.UpdatePhoneNumberCommand;
import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.query.MemberRepository;
import com.hedvig.memberservice.web.dto.InsuranceCancellationDTO;
import com.hedvig.memberservice.web.dto.InternalMember;
import com.hedvig.memberservice.web.dto.MemberCancelInsurance;
import com.hedvig.memberservice.web.dto.StartOnboardingWithSSNRequest;
import com.hedvig.memberservice.web.dto.UpdateContactInformationRequest;
import com.hedvig.memberservice.web.dto.UpdateEmailRequest;
import com.hedvig.memberservice.web.dto.UpdatePhoneNumberRequest;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.val;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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
@RequestMapping("/i/member")
public class InternalMembersController {

  private final Logger log = LoggerFactory.getLogger(InternalMembersController.class);
  private final CommandGateway commandBus;
  private final MemberRepository memberRepository;

  public InternalMembersController(CommandGateway commandBus, MemberRepository memberRepository) {
    this.commandBus = commandBus;
    this.memberRepository = memberRepository;
  }

  @GetMapping("/{memberId}")
  public ResponseEntity<InternalMember> index(@PathVariable Long memberId) {

    Optional<MemberEntity> member = memberRepository.findById(memberId);
    if (member.isPresent()) {

      return ResponseEntity.ok(InternalMember.fromEntity(member.get()));
    }

    return ResponseEntity.notFound().build();
  }

  @RequestMapping(value = "/{memberId}/finalizeOnboarding", method = RequestMethod.POST)
  public ResponseEntity<?> finalizeOnboarding(
      @PathVariable Long memberId, @RequestBody UpdateContactInformationRequest body) {

    MemberUpdateContactInformationCommand finalizeOnBoardingCommand =
        new MemberUpdateContactInformationCommand(memberId, body);

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
      @PathVariable Long memberId, @RequestBody UpdateEmailRequest request) {

    commandBus.sendAndWait(new UpdateEmailCommand(memberId, request.getEmail()));

    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/{memberId}/updatePhoneNumber")
  public ResponseEntity<?> updatePhoneNumber(@PathVariable Long memberId,
      @RequestBody UpdatePhoneNumberRequest request) {
    commandBus.sendAndWait(new UpdatePhoneNumberCommand(memberId, request.getPhoneNumber()));
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/search", method = RequestMethod.GET)
  @Transactional
  public Iterator<InternalMember> searchMembers(
      @RequestParam(name = "status", defaultValue = "", required = false) String status,
      @RequestParam(name = "query", defaultValue = "", required = false) String query) {

    status = status.trim();
    query = query.trim();
    try (val stream = search(status, query)) {
      return stream.map(InternalMember::fromEntity).collect(Collectors.toList()).iterator();
    }
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

  @PostMapping("{memberId}/edit")
  public void editMember(
      @PathVariable("memberId") String memberId,
      @RequestBody InternalMember dto,
      @RequestHeader("Authorization") String token) {

    Optional<MemberEntity> member = memberRepository.findById(Long.parseLong(memberId));

    if (member.isPresent() && !InternalMember.fromEntity(member.get()).equals(dto)) {
      commandBus.sendAndWait(new EditMemberInformationCommand(memberId, dto));
    }
  }

  @GetMapping("/many/[{memberIds}]")
  public ResponseEntity<List<InternalMember>> getMembers(
      @PathVariable("memberIds") List<Long> memberIds) {
    val members =
        memberRepository
            .findAllByIdIn(memberIds)
            .stream()
            .map(m -> InternalMember.fromEntity(m))
            .collect(Collectors.toList());

    if (memberIds.size() != members.size()) {
      log.error(
          "Length mismatch of supplied members and found members: wanted {}, found {}",
          memberIds.size(),
          members.size());
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(members);
  }

  private Stream<MemberEntity> search(String status, String query) {
    if (!query.equals("")) {
      Long memberId = parseMemberId(query);
      if (memberId != null) {
        if (!status.equals("")) {
          return memberRepository.searchByIdAndStatus(memberId, status);
        } else {
          return memberRepository.searchById(memberId);
        }
      }
    }

    if (!status.equals("") && !query.equals("")) {
      return memberRepository.searchByStatusAndQuery(status, query);
    }
    if (!status.equals("")) {
      return memberRepository.searchByStatus(status);
    }
    if (!query.equals("")) {
      return memberRepository.searchByQuery(query);
    }
    return memberRepository.searchAll();
  }

  private Long parseMemberId(String query) {
    try {
      return Long.parseLong(query);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
