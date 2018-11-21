package com.hedvig.memberservice.externalApi.notificationService;

import com.hedvig.memberservice.externalApi.notificationService.dto.CancellationEmailSentToInsurerRequest;
import com.hedvig.memberservice.externalApi.notificationService.dto.InsuranceActivationDateUpdatedRequest;
import org.springframework.stereotype.Component;

@Component
public class NotificationServiceImpl implements NotificationService {

  private final NotificationServiceClient notificationServiceClient;

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
}
