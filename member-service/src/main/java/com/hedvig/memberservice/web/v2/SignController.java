package com.hedvig.memberservice.web.v2;

import com.hedvig.memberservice.services.SigningService;
import com.hedvig.memberservice.web.v2.dto.SignStatusRequest;
import com.hedvig.memberservice.web.v2.dto.SignStatusResponse;
import com.hedvig.memberservice.web.v2.dto.WebSignResponse;
import com.hedvig.memberservice.web.v2.dto.WebsignRequest;
import javax.validation.Valid;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<?> websign(@RequestHeader("hedvig.token") final long hedvigToken, @RequestBody WebsignRequest websignRequest) {

    val result = signingService.startWebSign(hedvigToken, websignRequest);

    return ResponseEntity.ok(new WebSignResponse(result.getSignId(),result.getStatus(), result.getBankIdOrderResponse()));
  }

  @PostMapping("signStatus")
  public ResponseEntity<?> signStatus(@RequestHeader("hedvig.token") final long memberId,
      @Valid @RequestBody SignStatusRequest body) {


    val session = signingService.getSignStatus(memberId, body.getOrderRef());

    return session
        .map(x -> ResponseEntity.ok(SignStatusResponse.CreateFromEntity(x.getCollectResponse())))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

}
