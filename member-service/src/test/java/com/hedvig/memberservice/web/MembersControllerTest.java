package com.hedvig.memberservice.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
public class MembersControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Test
  public void givenACreatedMember_whenAssigningTrackingId_thenShouldGetTrackingId() {

  }
}

