package com.hedvig.memberservice.notificationService.queue.jobs;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.hedvig.memberservice.notificationService.queue.MemberBCCAddress;
import lombok.val;
import org.junit.Test;

public class MemberBCCAddressTest {

  @Test
  public void test1() {

    val sut = new MemberBCCAddress("test@hedvig.com");

    assertThat(sut.format("1234")).isEqualToIgnoringCase("test+1234@hedvig.com");

  }

}