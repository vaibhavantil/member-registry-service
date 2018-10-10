package com.hedvig.memberservice.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.hedvig.external.bankID.bankIdRest.BankIdRestApi;
import com.hedvig.external.bankID.bankIdRestTypes.OrderResponse;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BankIdRestServiceImplTest {

  @Mock
  BankIdRestApi api;

  @Test
  public void startSign_givenMemberIdAndSSNAndMessage_startsSign(){

    given(api.sign(any(), any(), any())).willReturn(new OrderResponse("orderRef", "autoStartToken"));

    BankIdRestServiceImpl sut = new BankIdRestServiceImpl(api);

    val result = sut.startSign("191212121212", "Some message", "127.0.0.1");

    assertThat(result.getAutoStartToken()).isEqualTo("autoStartToken");
    assertThat(result.getOrderRef()).isEqualTo("orderRef");

  }


}