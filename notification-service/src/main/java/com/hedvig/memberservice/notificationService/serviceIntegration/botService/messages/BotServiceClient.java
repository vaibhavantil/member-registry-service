package com.hedvig.memberservice.notificationService.serviceIntegration.botService.messages;

import com.fasterxml.jackson.databind.JsonNode;
import com.hedvig.memberservice.notificationService.serviceIntegration.botService.messages.dto.BackOfficeMessage;
import com.hedvig.memberservice.notificationService.serviceIntegration.botService.messages.dto.BackOfficeResponseDTO;
import com.hedvig.memberservice.notificationService.serviceIntegration.botService.messages.dto.PushTokenDTO;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "bot-service", url = "${hedvig.bot-service.location}")
public interface BotServiceClient {

  @GetMapping("/_/member/{hid}/push-token")
  PushTokenDTO getPushTokenByHid(
      @PathVariable("hid") String hid, @RequestHeader("Authorization") String token);

  @GetMapping("/_/v2/{memberId}/push-token")
  PushTokenDTO getFirebasePushTokenByMemberId(
      @PathVariable("memberId") String memberId, @RequestHeader("Authorization") String token);

  @GetMapping("/messages")
  JsonNode messages(
      @RequestHeader("hedvig.token") String hid, @RequestHeader("Authorization") String token);

  @GetMapping("/messages/{count}")
  JsonNode messages(
      @RequestHeader("hedvig.token") String hid,
      @PathVariable("count") int count,
      @RequestHeader("Authorization") String token);

  @GetMapping("/_/messages/{time}")
  List<BackOfficeMessage> fetch(
      @PathVariable("time") long time, @RequestHeader("Authorization") String token);

  @PostMapping("/_/messages/addmessage")
  void response(
      @RequestBody BackOfficeResponseDTO message, @RequestHeader("Authorization") String token);

  @PostMapping("/_/messages/addanswer")
  void answer(
      @RequestBody BackOfficeResponseDTO answer, @RequestHeader("Authorization") String token);
}
