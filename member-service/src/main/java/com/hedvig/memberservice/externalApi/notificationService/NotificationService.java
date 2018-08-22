package com.hedvig.memberservice.externalApi.notificationService;

import com.hedvig.memberservice.externalApi.notificationService.dto.CancellationEmailSentToInsurerRequest;
import com.hedvig.memberservice.externalApi.notificationService.dto.InsuranceActivationDateUpdatedRequest;

public interface NotificationService {

  void cancellationEmailSentToInsurer(Long memberId, CancellationEmailSentToInsurerRequest body);

  void insuranceActivated(Long memberId);

  void insuranceActivationDateUpdated(Long memberId, InsuranceActivationDateUpdatedRequest body);

  void insuranceReminder(int NumberOfDaysFromToday);
}
