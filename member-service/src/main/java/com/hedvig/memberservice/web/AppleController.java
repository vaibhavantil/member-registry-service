package com.hedvig.memberservice.web;

import com.hedvig.memberservice.commands.CreateMemberCommand;
import com.hedvig.memberservice.commands.InitializeAppleUserCommand;
import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.query.MemberRepository;
import com.hedvig.memberservice.web.dto.AppleInitializationRequest;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping({"/i/member"})
public class AppleController {

  @Value("${hedvig.appleUser.memberId}")
  private String APPLE_USER_MEMBER_ID;
  @Value("${hedvig.appleUser.personalNumber}")
  private String APPLE_USER_PERSONAL_NUMBER;
  @Value("${hedvig.appleUser.firstName}")
  private String APPLE_USER_FIRST_NAME;
  @Value("${hedvig.appleUser.lastName}")
  private String APPLE_USER_LAST_NAME;
  @Value("${hedvig.appleUser.address.street}")
  private String APPLE_USER_ADDRESS_STREET;
  @Value("${hedvig.appleUser.address.city}")
  private String APPLE_USER_ADDRESS_CITY;
  @Value("${hedvig.appleUser.address.zipCode}")
  private String APPLE_USER_ADDRESS_ZIP_CODE;
  @Value("${hedvig.appleUser.phoneNumber}")
  private String APPLE_USER_PHONE_NUMBER;
  @Value("${hedvig.appleUser.email}")
  private String APPLE_USER_EMAIL;

  private final MemberRepository memberRepository;
  private final CommandGateway commandGateway;

  public AppleController(MemberRepository memberRepository, CommandGateway commandGateway) {
    this.memberRepository = memberRepository;
    this.commandGateway = commandGateway;
  }

  @PostMapping("/initAppleUser")
  public ResponseEntity<Void> intitiateAppleUser(@RequestBody AppleInitializationRequest request) {
    if (!APPLE_USER_MEMBER_ID.equals(request.getMemberId())) {
      return ResponseEntity.badRequest().build();
    }

    long appleMemberId = Long.parseLong(APPLE_USER_MEMBER_ID);

    Optional<MemberEntity> memberEntityMaybe = memberRepository.findById(appleMemberId);

    if (memberEntityMaybe.isPresent()){
      return ResponseEntity.badRequest().build();
    }

    commandGateway.sendAndWait(new CreateMemberCommand(appleMemberId));

    InitializeAppleUserCommand cmd = new InitializeAppleUserCommand(
      appleMemberId,
      APPLE_USER_PERSONAL_NUMBER,
      APPLE_USER_FIRST_NAME,
      APPLE_USER_LAST_NAME,
      APPLE_USER_PHONE_NUMBER,
      APPLE_USER_EMAIL,
      APPLE_USER_ADDRESS_STREET,
      APPLE_USER_ADDRESS_CITY,
      APPLE_USER_ADDRESS_ZIP_CODE
    );

    commandGateway.sendAndWait(cmd);

    return ResponseEntity.noContent().build();
  }
}
