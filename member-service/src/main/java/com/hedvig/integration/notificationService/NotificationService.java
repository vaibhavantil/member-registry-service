package com.hedvig.integration.notificationService;

import com.hedvig.integration.notificationService.dto.CancellationEmailSentToInsurerRequest;
import com.hedvig.integration.notificationService.dto.InsuranceActivationDateUpdatedRequest;

public interface NotificationService {

  void cancellationEmailSentToInsurer(Long memberId, CancellationEmailSentToInsurerRequest body);

  void insuranceActivated(Long memberId);

  void insuranceActivationDateUpdated(Long memberId, InsuranceActivationDateUpdatedRequest body);

  void insuranceReminder(int NumberOfDaysFromToday);
}
