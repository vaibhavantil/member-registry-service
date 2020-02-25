package com.hedvig.integration.botService;

import com.hedvig.integration.botService.dto.UpdateUserContextDTO;
import com.hedvig.integration.productsPricing.dto.EditMemberNameRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BotServiceImpl implements BotService {

  private final Logger logger = LoggerFactory.getLogger(BotServiceImpl.class);
  private final BotServiceClient botServiceClient;

  @Autowired
  public BotServiceImpl(BotServiceClient botServiceClient) {
    this.botServiceClient = botServiceClient;
  }

  @Override
  public void initBotServiceSessionWebOnBoarding(Long memberId, UpdateUserContextDTO userContext) {
    logger.info("Start updating context in bot-service");
    botServiceClient.initBotServiceSessionWebOnBoarding(memberId, userContext);
  }

  @Override
  public void initBotService(Long memberId, String json) {
    logger.info("Initializing bot-service by helloHedvig request");

    if (json == null) {
      botServiceClient.initBotService(memberId);
    } else {
      //botServiceClient.initBotService(memberId, json);
    }
  }

  public void editMemberName(String memberId, EditMemberNameRequestDTO editMemberNameRequestDTO) {
    botServiceClient.editMemberName(memberId, editMemberNameRequestDTO);
  }
}
