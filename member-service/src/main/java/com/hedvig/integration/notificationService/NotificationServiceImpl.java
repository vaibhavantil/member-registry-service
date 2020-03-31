package com.hedvig.integration.notificationService;

import com.hedvig.integration.notificationService.dto.CancellationEmailSentToInsurerRequest;
import com.hedvig.integration.notificationService.dto.InsuranceActivationDateUpdatedRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

  private final NotificationServiceClient notificationServiceClient;

  @Autowired
  public NotificationServiceImpl(NotificationServiceClient notificationServiceClient) {
    this.notificationServiceClient = notificationServiceClient;
  }

  @Override
  public void cancellationEmailSentToInsurer(
      Long memberId, CancellationEmailSentToInsurerRequest request) {
    notificationServiceClient.cancellationEmailSentToInsurer(memberId, request);
  }

  @Override
  public void insuranceActivated(Long memberId) {
    notificationServiceClient.insuranceActivated(memberId);
  }

  @Override
  public void insuranceActivationDateUpdated(
      Long memberId, InsuranceActivationDateUpdatedRequest request) {
    notificationServiceClient.insuranceActivationDateUpdated(memberId, request);
  }

  @Override
  public void insuranceReminder(int NumberOfDaysFromToday) {
    notificationServiceClient.insuranceReminder(NumberOfDaysFromToday);
  }

  @Override
  public void deleteCustomer(@NotNull String memberId) {
    notificationServiceClient.deleteCustomer(memberId);
  }
}
