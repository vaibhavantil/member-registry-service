package com.hedvig.memberservice.entities;


import static org.assertj.core.api.Assertions.assertThat;

import com.hedvig.external.bankID.bankIdTypes.CollectStatus;
import com.hedvig.external.bankID.bankIdTypes.OrderResponse;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SignSessionTest {

  private static final long MEMBER_ID = 1337L;
  private static final String ORDER_REFERENCE = "orderReference";
  private static final String AUTO_START_TOKEN = "autoStartToken";

  @Test
  public void canReuseBankIdSession_givenNoBankIdSession_returnTrue() {
    val session = new SignSession(MEMBER_ID);
    val actual = session.canReuseBankIdSession();
    assertThat(actual).isFalse();
  }

  @Test
  public void canReuseBankIdSession_givenBankIdSessionNoCollectReponse_returnTrue() {
    val session = new SignSession(MEMBER_ID);
    session.newOrderStarted(new OrderResponse(ORDER_REFERENCE, AUTO_START_TOKEN));

    val actual = session.canReuseBankIdSession();
    assertThat(actual).isTrue();
  }

  @Test
  public void canReuseBankIdSession_givenPendingBankIdSession_returnTrue() {
    val session = new SignSession(MEMBER_ID);

    val collectResponse = new CollectResponse();
    collectResponse.setStatus(CollectStatus.pending);
    session.newCollectResponse(collectResponse);

    val actual = session.canReuseBankIdSession();

    assertThat(actual).isTrue();
  }

  @Test
  public void canReuseBankIdSession_givenCompleteBankIdSession_returnFalse() {
    val session = new SignSession(MEMBER_ID);

    val collectResponse = new CollectResponse();
    collectResponse.setStatus(CollectStatus.complete);
    session.newCollectResponse(collectResponse);

    val actual = session.canReuseBankIdSession();

    assertThat(actual).isFalse();
  }

  @Test
  public void canReuseBankIdSession_givenFailedBankIdSession_returnFalse() {
    val session = new SignSession(MEMBER_ID);

    val collectResponse = new CollectResponse();
    collectResponse.setStatus(CollectStatus.failed);
    session.newCollectResponse(collectResponse);

    val actual = session.canReuseBankIdSession();

    assertThat(actual).isFalse();
  }
}
