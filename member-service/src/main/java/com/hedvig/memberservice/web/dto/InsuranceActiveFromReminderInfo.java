package com.hedvig.memberservice.web.dto;

import com.hedvig.memberservice.externalApi.productsPricing.dto.InsuranceNotificationDTO;
import com.hedvig.memberservice.query.projections.MemberMailInfo;
import java.time.LocalDate;
import lombok.Value;

@Value
public class InsuranceActiveFromReminderInfo {
  private Long memberId;
  private String email;
  private LocalDate activeFrom;

  public InsuranceActiveFromReminderInfo(
      InsuranceNotificationDTO insurance, MemberMailInfo memberInfo) {
    this.memberId = memberInfo.getId();
    this.email = memberInfo.getEmail();
    this.activeFrom =
        insurance.getActivationDate().toLocalDate(); // TODO: NOT SURE MAYBE SWEDISH TIME ?
  }
}
