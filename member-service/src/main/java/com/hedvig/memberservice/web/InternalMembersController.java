package com.hedvig.memberservice.web;

import com.hedvig.auth.services.UserService;
import com.hedvig.memberservice.aggregates.PickedLocale;
import com.hedvig.memberservice.commands.*;
import com.hedvig.memberservice.identity.repository.IdentityRepository;
import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.query.MemberRepository;
import com.hedvig.memberservice.services.member.MemberQueryService;
import com.hedvig.memberservice.services.trace.TraceMemberService;
import com.hedvig.memberservice.web.dto.*;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.eventstore.jpa.DomainEventEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

            InternalMember internalMember = InternalMember.Companion.fromEntity(member.get());
            internalMember.getTraceMemberInfo().addAll(traceMemberService.getTracesByMemberId(memberId));
            return ResponseEntity.ok(internalMember);
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

    //THIS is only used by bot service don't use
    @Deprecated
    @RequestMapping(value = "/{memberId}/startOnboardingWithSSN", method = RequestMethod.POST)
    public ResponseEntity<?> startOnboardingWithSSN(
        @PathVariable Long memberId, @RequestBody StartOnboardingWithSSNRequest request) {

        try {
            commandBus.sendAndWait(new StartSwedishOnboardingWithSSNCommand(memberId, request));
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

    @RequestMapping(value = "/{memberId}/updatePhoneNumber", method = RequestMethod.POST)
    public ResponseEntity<?> updatePhoneNumber(@PathVariable Long memberId,
                                               @RequestBody UpdatePhoneNumberRequest request) {
        commandBus.sendAndWait(new UpdatePhoneNumberCommand(memberId, request.getPhoneNumber()));
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public List<InternalMember> searchMembers(
        @RequestParam(name = "includeAll", defaultValue = "", required = false) @Nullable Boolean includeAll,
        @RequestParam(name = "query", defaultValue = "", required = false) String query) {

        query = query.trim();

        InternalMemberSearchRequestDTO req = new InternalMemberSearchRequestDTO();
        req.setQuery(query);
        if (includeAll != null) {
            req.setIncludeAll(includeAll);
        }

        return memberQueryService.search(req).getMembers();
    }

    @RequestMapping(value = "/searchPaged", method = RequestMethod.GET)
    public InternalMemberSearchResultDTO searchMembersPaged(InternalMemberSearchRequestDTO req) {
        return memberQueryService.search(req);
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
        @RequestHeader("Authorization") String token
    ) {

        Optional<MemberEntity> member = memberRepository.findById(Long.parseLong(memberId));

        if (member.isPresent() && !InternalMember.Companion.fromEntity(member.get()).equals(dto)) {
            commandBus.sendAndWait(new EditMemberInformationCommand(memberId, dto, token));
        }
    }

    @PostMapping("/edit/info")
    public void editMemberInfo(
        @RequestBody EditMemberInfoRequest request,
        @RequestHeader("Authorization") String token
    ) {
        commandBus.sendAndWait(EditMemberInfoCommand.Companion.from(request, token));
    }

    @PostMapping(value = "/{memberId}/updateSSN")
    public ResponseEntity<Void> updateSSN(
        @PathVariable Long memberId,
        @RequestBody UpdateSSNRequest request
    ) {
        commandBus.sendAndWait(new UpdateSSNCommand(memberId, request.getSsn(), request.getNationality()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{memberId}/pickedLocale")
    public ResponseEntity<PickedLocaleDTO> getPickedLocale(@PathVariable Long memberId) {

        Optional<MemberEntity> member = memberRepository.findById(memberId);
        if (member.isPresent() && member.get().pickedLocale != null) {

            PickedLocaleDTO res = new PickedLocaleDTO(member.get().pickedLocale);
            return ResponseEntity.ok(res);
        }
        //TODO: Returning sv_SE per default if member not found for launch in order to not block
        // notification service.
        PickedLocaleDTO res = new PickedLocaleDTO(PickedLocale.sv_SE);
        return ResponseEntity.ok(res);

        //TODO: Replace with this after launch:
        //return ResponseEntity.notFound().build();

    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Boolean> deleteMember(@PathVariable Long memberId) {
        commandBus.sendAndWait(new DeleteMemberCommand(memberId));
        return ResponseEntity.ok(true);
    }
}

