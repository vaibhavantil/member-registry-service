package com.hedvig.memberservice.externalApi.botService;

import com.hedvig.memberservice.externalApi.botService.dto.UpdateUserContextDTO;

public interface BotService {
  void updateContextWebOnBoarding(Long memberId, UpdateUserContextDTO userContext);
}
