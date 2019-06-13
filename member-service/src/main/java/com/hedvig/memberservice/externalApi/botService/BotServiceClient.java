package com.hedvig.memberservice.externalApi.botService;

import com.hedvig.memberservice.externalApi.botService.dto.UpdateUserContextDTO;
import com.hedvig.memberservice.externalApi.productsPricing.dto.EditMemberNameRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "bot-service",
    url = "${hedvig.botservice.baseurl:bot-service}"
)
public interface BotServiceClient {

 @PostMapping("/_/member/{memberId}/initSessionWebOnBoarding")
  ResponseEntity<?> initBotServiceSessionWebOnBoarding(@PathVariable(name = "memberId") Long memberId, @RequestBody UpdateUserContextDTO req);

  @PostMapping("/_/member/{memberId}/editMemberName")
  ResponseEntity<EditMemberNameRequestDTO> editMemberName(
    @PathVariable ("memberId") String memberId,
    @RequestBody EditMemberNameRequestDTO dto
  );
}
