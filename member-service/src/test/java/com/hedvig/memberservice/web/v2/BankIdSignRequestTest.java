package com.hedvig.memberservice.web.v2;

import com.hedvig.memberservice.web.dto.BankIdSignRequest;
import lombok.val;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BankIdSignRequestTest {
  @Test
  public void test_trimming_for_ssn() {
    val request = new BankIdSignRequest("19221212-1212", "TestMe", "12345");
    assertThat(request.getSsn().contains("-")).isFalse();
  }
}
