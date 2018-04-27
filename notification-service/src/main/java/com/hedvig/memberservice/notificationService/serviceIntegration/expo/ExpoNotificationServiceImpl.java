package com.hedvig.memberservice.notificationService.serviceIntegration.expo;

import com.hedvig.memberservice.notificationService.serviceIntegration.botService.messages.BotService;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExpoNotificationServiceImpl implements ExpoNotificationService {

    private static Logger logger = LoggerFactory.getLogger(ExpoNotificationServiceImpl.class);

    private final BotService botService;
    private final ExpoClient expoClient;

    public ExpoNotificationServiceImpl(BotService botService, ExpoClient expoClient) {
        this.botService = botService;
        this.expoClient = expoClient;
    }

    @Override
    public void sendNotification(String hid, String message) {
        try {
            val expoId = botService.pushTokenId(hid, "");
            val dto = new ExpoPushDTO(
                expoId,
                "Hedvig",
                message
            );
            logger.info("Attempting  to send push to user with id: {}, body: {}", hid, dto.toString());
            val result = expoClient.sendPush(dto);
            logger.info("Got result from expo for push notification to user with id: {}, body: {}", hid, result);
        } catch (Exception e) {
            logger.error("Error, could not send push notification through expo", e);
        }
    }
}
