package com.hedvig.memberservice.notificationService.serviceIntegration.firebase;

import java.io.IOException;
import java.util.Arrays;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.hedvig.memberservice.notificationService.serviceIntegration.firebase.dto.FCMRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import lombok.val;

public class FirebaseNotificationServiceImpl implements FirebaseNotificationService {
  public static final String TITLE = "Hedvig";
  public static final String BODY = "Hej, du har ett nytt meddelande från Hedvig!";
  private final FirebaseClient firebaseClient;
  private Resource firebaseCredentials;

  public FirebaseNotificationServiceImpl(FirebaseClient firebaseClient, @Value("classpath:service-account.json") Resource firebaseCredentials) {
    this.firebaseClient = firebaseClient;
    this.firebaseCredentials = firebaseCredentials;
  }

	@Override
	public void sendNotification(String fcmToken) {
    val request = new FCMRequest(fcmToken, TITLE, BODY);
    firebaseClient.sendPush(request, getFirebaseAccessToken());
  }

  private String getFirebaseAccessToken() {
    try (val in = firebaseCredentials.getInputStream()) {
      val credential = GoogleCredential
        .fromStream(in)
        .createScoped(Arrays.asList("https://www.googleapis.com/auth/firebase.messaging"));
      credential.refreshToken();
      return credential.getAccessToken();
    } catch (IOException e) {
      throw new RuntimeException("Could not open firebase credentials: ", e);
    }
  }
}
