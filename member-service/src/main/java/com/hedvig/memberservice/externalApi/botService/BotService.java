package com.hedvig.memberservice.externalApi.botService;

import com.hedvig.memberservice.externalApi.botService.dto.UpdateUserContextDTO;
import com.hedvig.memberservice.externalApi.productsPricing.dto.EditMemberNameRequestDTO;

public interface BotService {
  void initBotServiceSessionWebOnBoarding(Long memberId, UpdateUserContextDTO userContext);

  void editMemberName(String memberId, EditMemberNameRequestDTO editMemberNameRequestDTO);
}
