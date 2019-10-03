package com.hedvig.memberservice.web.v2;

import com.hedvig.memberservice.services.SigningService;
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

    return ResponseEntity.ok(new WebSignResponse(result.getSignId(),result.getStatus(), result.getBankIdOrderResponse()));
  }

  @PostMapping("signQuotesFromUnderwriter")
  public ResponseEntity<UnderwriterQuoteSignResponse> signQuotesFromUnderwriter(
    @RequestHeader("hedvig.token") final long hedvigToken, @RequestBody UnderwriterQuoteSignRequest underwriterQuoteSignRequest
  ) {
     val result = signingService.signUnderwriterQuote(hedvigToken, underwriterQuoteSignRequest);
    return ResponseEntity.ok(new UnderwriterQuoteSignResponse(result.getSignId(),result.getMemberIsSigned()));
  }

  @GetMapping("signStatus")
  public ResponseEntity<SignStatusResponse> signStatus(@RequestHeader("hedvig.token") final long memberId) {

    val session = signingService.getSignStatus(memberId);

    return session
        .map(x -> ResponseEntity.ok(SignStatusResponse.CreateFromEntity(x)))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

}
