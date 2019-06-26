package com.hedvig.integration.botService;

import com.hedvig.integration.botService.dto.UpdateUserContextDTO;
import com.hedvig.integration.productsPricing.dto.EditMemberNameRequestDTO;

public interface BotService {
  void initBotServiceSessionWebOnBoarding(Long memberId, UpdateUserContextDTO userContext);

  void editMemberName(String memberId, EditMemberNameRequestDTO editMemberNameRequestDTO);
}
