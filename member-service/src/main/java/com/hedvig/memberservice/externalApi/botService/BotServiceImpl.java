package com.hedvig.memberservice.externalApi.botService;

import com.hedvig.memberservice.externalApi.botService.dto.UpdateUserContextDTO;
import com.hedvig.memberservice.externalApi.productsPricing.dto.EditMemberNameRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BotServiceImpl implements BotService {

  private final Logger logger = LoggerFactory.getLogger(BotServiceImpl.class);
  private final BotServiceClient botServiceClient;

  public BotServiceImpl(BotServiceClient botServiceClient) {
    this.botServiceClient = botServiceClient;
  }

  @Override
  public void initBotServiceSessionWebOnBoarding(Long memberId, UpdateUserContextDTO userContext) {
    logger.info("Start updating context in bot-service");
    botServiceClient.initBotServiceSessionWebOnBoarding(memberId, userContext);
  }

  public void editMemberName(String memberId, EditMemberNameRequestDTO editMemberNameRequestDTO) {
    botServiceClient.editMemberName(memberId, editMemberNameRequestDTO);
  }
}
