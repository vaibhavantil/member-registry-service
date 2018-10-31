package com.hedvig.memberservice.web;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.any;

import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.memberservice.TestApplication;
import com.hedvig.memberservice.commands.AssignTrackingIdCommand;
import com.hedvig.memberservice.externalApi.productsPricing.ProductApi;
import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.query.MemberRepository;
import com.hedvig.memberservice.query.TrackingIdRepository;
import com.hedvig.memberservice.services.CashbackService;
import com.hedvig.memberservice.web.dto.TrackingIdDto;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestApplication.class)
@WebMvcTest(controllers = MembersController.class)
public class MembersControllerTest {
  @Autowired
  private MockMvc mockMvc;
  @MockBean
  MemberRepository memberRepository;
  @MockBean
  CommandGateway commandGateway;
  @MockBean
  RetryTemplate retryTemplate;
  @MockBean
  ProductApi productApi;
  @MockBean
  CashbackService cashbackService;
  @MockBean
  TrackingIdRepository trackingIdRepository;

  @Autowired
  ObjectMapper objectMapper;

  private static final UUID TRACKING_ID = UUID.fromString("2e11fac8-7417-43e8-8446-c4447cb22fea");
  private static final long MEMBER_ID = 111111l;

  @Test
  public void givenACreatedMember_whenAssigningTrackingId_thenShouldGetTrackingId() throws Exception {
    when(memberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(new MemberEntity()));
    mockMvc
        .perform(post("/member/trackingId", "").content(objectMapper.writeValueAsString(new TrackingIdDto(TRACKING_ID)))
            .contentType(MediaType.APPLICATION_JSON).header("hedvig.token", Long.toString(MEMBER_ID)))
        .andExpect(status().isOk());

    then(commandGateway).should(times(1)).sendAndWait(new AssignTrackingIdCommand(MEMBER_ID, TRACKING_ID));
  }

  @Test
  public void givenANonExistentMember_whenAssigningTrackingId_thenShouldReturnNotFound() throws Exception {
    when(memberRepository.findById(MEMBER_ID)).thenReturn(Optional.empty());

    mockMvc
        .perform(post("/member/trackingId", "").content(objectMapper.writeValueAsString(new TrackingIdDto(TRACKING_ID)))
            .contentType(MediaType.APPLICATION_JSON).header("hedvig.token", Long.toString(MEMBER_ID)))
        .andExpect(status().isNotFound());

    then(commandGateway).should(times(0)).sendAndWait(any());
  }
}
