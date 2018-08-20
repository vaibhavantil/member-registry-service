package com.hedvig.memberservice.notificationService.serviceIntegration.botService.messages;

import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BotServiceImpl implements BotService {

    private static Logger logger = LoggerFactory.getLogger(BotServiceImpl.class);

    final private BotServiceClient botServiceClient;

    public BotServiceImpl(BotServiceClient botServiceClient) {
        this.botServiceClient = botServiceClient;
    }


    @Override
  public String pushTokenId(String hid, String token) {
        val pushTokenDto = botServiceClient.getPushTokenByHid(hid, token);
        return pushTokenDto.getToken();
  }
}
