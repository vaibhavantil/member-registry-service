package com.hedvig.memberservice.entities;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.val;
import org.junit.Test;

public class BankIdSessionTest {

  @Test
  public void canBeReused_givenNoCollectData_thenReturnTrue(){
    val session = new BankIdSession();

    assertThat(session.canBeReused()).isTrue();
  }
}
