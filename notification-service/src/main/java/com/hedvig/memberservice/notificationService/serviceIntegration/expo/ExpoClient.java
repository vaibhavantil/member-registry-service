package com.hedvig.memberservice.notificationService.serviceIntegration.expo;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name="expo", url="https://exp.host/--/api/v2")
public interface ExpoClient {
    @RequestMapping(method = RequestMethod.POST, value="/push/send")
    String sendPush(ExpoPushDTO push);
}
