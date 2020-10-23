package com.hedvig.integration.productsPricing;

import com.hedvig.integration.productsPricing.dto.*;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@FeignClient(name = "productPricing", url = "${hedvig.productsPricing.url:product-pricing}")
public interface ProductClient {

  @RequestMapping(value = "/i/campaign/member/create", method = RequestMethod.POST)
  ResponseEntity<Void> createdCampaignMember(@RequestBody MemberCreatedRequest req);

  @RequestMapping(value = "/i/campaign/member/update/name", method = RequestMethod.POST)
  ResponseEntity<Void> updateCampaignMemberName(@RequestBody MemberNameUpdateRequest req);

  @GetMapping("/_/contracts/members/{memberId}/hasContract")
  ResponseEntity<Boolean> hasContract(@PathVariable("memberId") Long memberId);
}
