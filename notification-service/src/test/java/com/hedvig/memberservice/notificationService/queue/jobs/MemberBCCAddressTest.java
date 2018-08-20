package com.hedvig.memberservice.notificationService.queue.jobs;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.hedvig.memberservice.notificationService.queue.MemberBCCAddress;
import lombok.val;
import org.junit.Test;

public class MemberBCCAddressTest {

  private static final String ILLEGAL_EMAIL_ADDRESS = "test@hedvig@h.com";

  @Test
  public void test1() {

    val sut = new MemberBCCAddress("test@hedvig.com");

    assertThat(sut.format("1234")).isEqualToIgnoringCase("test+1234@hedvig.com");

  }

  @Test
  public void create_withInvalidEmailAddress_throwsException() {

    assertThatThrownBy(() -> new MemberBCCAddress(ILLEGAL_EMAIL_ADDRESS)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(ILLEGAL_EMAIL_ADDRESS);

  }


}