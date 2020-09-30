package com.hedvig.memberservice.web.v2.dto;

import com.hedvig.external.bankID.bankIdTypes.OrderResponse;
import com.hedvig.memberservice.entities.SignStatus;
import com.hedvig.memberservice.services.member.dto.DanishBankIdResponse;
import com.hedvig.memberservice.services.member.dto.NorwegianBankIdResponse;
import lombok.NonNull;
import lombok.Value;

@Value
public class WebSignResponse {

  @NonNull
  Long signId;
  SignStatus status;
  OrderResponse bankIdOrderResponse;
  NorwegianBankIdResponse norwegianSignResponse;
  DanishBankIdResponse danishBankIdResponse;
}
