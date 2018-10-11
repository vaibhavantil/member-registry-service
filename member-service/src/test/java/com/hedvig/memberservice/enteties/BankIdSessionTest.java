package com.hedvig.memberservice.enteties;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BankIdSessionTest {

  @Test
  public void canBeReused_givenNoCollectData_thenReturnTrue(){
    val session = new BankIdSession();

    assertThat(session.canBeReused()).isTrue();
  }



}