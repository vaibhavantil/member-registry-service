package com.hedvig.memberservice.web.v2;

import com.hedvig.memberservice.services.SigningService;
import com.hedvig.memberservice.services.member.dto.ErrorCodes;
import com.hedvig.memberservice.services.member.dto.ErrorResponseDto;
import com.hedvig.memberservice.web.dto.IsMemberAlreadySignedResponse;
import com.hedvig.memberservice.web.dto.IsSsnAlreadySignedMemberResponse;
import com.hedvig.memberservice.web.v2.dto.*;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/member/sign")
public class SignController {

  private final SigningService signingService;

  @Autowired
  SignController(SigningService signingService) {
    this.signingService = signingService;
  }


  @PostMapping("websign")
  public ResponseEntity<WebSignResponse> websign(@RequestHeader("hedvig.token") final long hedvigToken, @RequestBody WebsignRequest websignRequest) {

    val result = signingService.startWebSign(hedvigToken, websignRequest);

    return ResponseEntity.ok(new WebSignResponse(result.getSignId(),result.getStatus(), result.getBankIdOrderResponse(), result.getNorwegianBankIdResponse()));
  }

  @PostMapping("underwriter")
  public ResponseEntity<?> signQuotesFromUnderwriter(
    @RequestHeader("hedvig.token") final long memberId,
    @RequestBody UnderwriterQuoteSignRequest underwriterQuoteSignRequest) {

    try {
      val result = signingService.signUnderwriterQuote(memberId, underwriterQuoteSignRequest);
      return ResponseEntity.ok(new UnderwriterQuoteSignResponse(result.getSignId(),result.getMemberIsSigned()));
    } catch(Exception exception) {
      return ResponseEntity.status(422).body(new ErrorResponseDto(ErrorCodes.MEMBER_HAS_EXISTING_INSURANCE, "Not able to sign quote, the social security given is already associated with a signed product"));
    }
  }

  @GetMapping("signedSSN")
  public ResponseEntity<IsSsnAlreadySignedMemberResponse> isSsnAlreadySigned(@RequestHeader String ssn) {

    val isSsnAlreadySignedMember = signingService.isSsnAlreadySignedMember(ssn);
    return ResponseEntity.ok(isSsnAlreadySignedMember);
  }

  @GetMapping("signedMember")
  public ResponseEntity<IsMemberAlreadySignedResponse> isMemberAlreadySigned(@RequestHeader Long memberId) {

    val isMemberAlreadySigned = signingService.isMemberAlreadySigned(memberId);
    return ResponseEntity.ok(isMemberAlreadySigned);
  }

  @GetMapping("signStatus")
  public ResponseEntity<SignStatusResponse> signStatus(@RequestHeader("hedvig.token") final long memberId) {

    val session = signingService.getSignStatus(memberId);

    if (session != null) {
      return ResponseEntity.ok(session);
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}
