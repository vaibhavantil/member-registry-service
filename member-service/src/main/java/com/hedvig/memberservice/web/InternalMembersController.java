package com.hedvig.memberservice.web;

import com.hedvig.memberservice.commands.MemberUpdateContactInformationCommand;
import com.hedvig.memberservice.commands.StartOnboardingWithSSNCommand;
import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.query.MemberRepository;
import com.hedvig.memberservice.web.dto.InternalMember;
import com.hedvig.memberservice.web.dto.StartOnboardingWithSSNRequest;
import com.hedvig.memberservice.web.dto.UpdateContactInformationRequest;
import lombok.val;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/i/member")
public class InternalMembersController {

    private final CommandGateway commandBus;
    private final MemberRepository memberRepository;

    public InternalMembersController(
            CommandGateway commandBus,
            MemberRepository memberRepository) {
        this.commandBus = commandBus;
        this.memberRepository = memberRepository;
    }


    @RequestMapping(value = "/{memberId}/finalizeOnboarding", method = RequestMethod.POST)
    public ResponseEntity<?> finalizeOnboarding(@PathVariable Long memberId, @RequestBody UpdateContactInformationRequest body) {

        MemberUpdateContactInformationCommand finalizeOnBoardingCommand = new MemberUpdateContactInformationCommand(memberId, body);

        commandBus.sendAndWait(finalizeOnBoardingCommand);

        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/{memberId}/startOnboardingWithSSN", method = RequestMethod.POST)
    public ResponseEntity<?> startOnboardingWithSSN(@PathVariable Long memberId, @RequestBody StartOnboardingWithSSNRequest request) {


        Optional<MemberEntity> member = memberRepository.findBySsn(request.getSsn());
        if (member.isPresent()) {
            return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body(member.get().getId().toString());
        }

        commandBus.sendAndWait(new StartOnboardingWithSSNCommand(memberId, request));

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
            return stream
                    .map(InternalMember::fromEnity)
                    .collect(Collectors.toList())
                    .iterator();
        }
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
