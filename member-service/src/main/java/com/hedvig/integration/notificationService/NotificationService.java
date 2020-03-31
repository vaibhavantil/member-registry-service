package com.hedvig.integration.notificationService;

import com.hedvig.integration.notificationService.dto.CancellationEmailSentToInsurerRequest;
import com.hedvig.integration.notificationService.dto.InsuranceActivationDateUpdatedRequest;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface NotificationService {

  void cancellationEmailSentToInsurer(Long memberId, CancellationEmailSentToInsurerRequest body);

  void insuranceActivated(Long memberId);

  void insuranceActivationDateUpdated(Long memberId, InsuranceActivationDateUpdatedRequest body);

  void insuranceReminder(int NumberOfDaysFromToday);

  void deleteCustomer(@NotNull String memberId);

  void updateCustomer(@NotNull String memberId, @NotNull Map<String, Object> traitsMap);
}
