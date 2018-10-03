package com.hedvig.memberservice.web.v2;

import com.hedvig.memberservice.services.MemberService;
import com.hedvig.memberservice.web.v2.dto.WebSignResponse;
import com.hedvig.memberservice.web.v2.dto.WebsignRequest;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/member")
public class MemberController {



  final MemberService memberService;

  @Autowired
  MemberController(MemberService memberService) {
    this.memberService = memberService;
  }


  @PostMapping("websign")
  public ResponseEntity<?> websign(@RequestHeader("hedvig.token") final long hedvigToken, @RequestBody WebsignRequest websignRequest) {

    val result = memberService.startWebSign(hedvigToken, websignRequest);

    return ResponseEntity.ok(new WebSignResponse(result.getBankIdOrderResponse()));

  }

}
