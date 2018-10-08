package com.hedvig.memberservice.externalApi.botService;

import com.hedvig.memberservice.externalApi.botService.dto.UpdateUserContextDTO;
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

 @PostMapping("/_/member/{memberId}/updateContextWebOnBoarding")
  ResponseEntity<?> updateContextWebOnBoarding(@PathVariable(name = "memberId") Long memberId, @RequestBody UpdateUserContextDTO req);

}
