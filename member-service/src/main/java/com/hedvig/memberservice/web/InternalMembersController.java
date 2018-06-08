package com.hedvig.memberservice.web;

import com.hedvig.memberservice.commands.*;
import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.services.MemberService;
import com.hedvig.memberservice.web.dto.*;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/i/member")
public class InternalMembersController {

  private static final String PAGESIZE = "25";
  private final Logger log = LoggerFactory.getLogger(InternalMembersController.class);
  private final CommandGateway commandBus;
  private final MemberService memberService;

  public InternalMembersController(CommandGateway commandBus, MemberService memberService) {
    this.commandBus = commandBus;
    this.memberService = memberService;
  }

  @GetMapping("/{memberId}")
  public ResponseEntity<InternalMember> index(@PathVariable Long memberId) {

    Optional<MemberEntity> member = memberService.findMemberById(memberId);

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

  @RequestMapping(value = "/search", method = RequestMethod.GET)
  public Page<InternalMember> searchMembers(
      @RequestParam(name = "status", defaultValue = "", required = false) String status,
      @RequestParam(name = "query", defaultValue = "", required = false) String query,
      @RequestParam(name = "pageNumber", defaultValue = "1", required = false) int pageNumber,
      @RequestParam(name = "pageSize", defaultValue = PAGESIZE, required = false) int pageSize,
      @RequestParam(name = "direction", defaultValue = "ASC", required = false) String direction,
      @RequestParam(name = "orderBy", defaultValue = "id", required = false) String orderBy) {

    status = status.trim();
    query = query.trim();
    direction = direction.trim();
    orderBy = orderBy.trim();

    return memberService
        .search( status, query,
            new PageRequest(pageNumber - 1, pageSize, Direction.fromString(direction), orderBy))
        .map(InternalMember::fromEntity);
  }

  @RequestMapping(value = "/listAll", method = RequestMethod.GET)
  public Page<InternalMember> listAllMembers(
      @RequestParam(name = "pageNumber", defaultValue = "1", required = false) int pageNumber,
      @RequestParam(name = "pageSize", defaultValue = PAGESIZE, required = false) int pageSize,
      @RequestParam(name = "direction", defaultValue = "ASC", required = false) String direction,
      @RequestParam(name = "orderBy", defaultValue = "id", required = false) String orderBy) {

    direction.trim();
    orderBy.trim();

    return memberService
        .listAllMembers(
            new PageRequest(pageNumber - 1, pageSize, Direction.fromString(direction), orderBy))
        .map(InternalMember::fromEntity);
  }

  @RequestMapping(value = "/{memberId}/memberCancelInsurance", method = RequestMethod.POST)
  public ResponseEntity<?> memberCancelInsurance(
      @PathVariable Long memberId, @RequestBody MemberCancelInsurance body) {
    log.info("Dispatching MemberCancelInsuranceCommand for member ({})", memberId);
    commandBus.sendAndWait(new MemberCancelInsuranceCommand(memberId, body.getCancellationDate()));
    return ResponseEntity.accepted().build();
  }

  @PostMapping("{memberId}/edit")
  public void editMember(
      @PathVariable("memberId") String memberId,
      @RequestBody InternalMember dto,
      @RequestHeader("Authorization") String token) {

    Optional<MemberEntity> member = memberService.findMemberById(Long.parseLong(memberId));

    if (member.isPresent() && !InternalMember.fromEntity(member.get()).equals(dto)) {
      commandBus.sendAndWait(new EditMemberInformationCommand(memberId, dto));
    }
  }
}
