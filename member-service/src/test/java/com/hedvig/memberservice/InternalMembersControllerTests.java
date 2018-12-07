package com.hedvig.memberservice;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.memberservice.aggregates.FraudulentStatus;
import com.hedvig.memberservice.commands.MemberUpdateContactInformationCommand;
import com.hedvig.memberservice.commands.SetFraudulentStatusCommand;
import com.hedvig.memberservice.commands.StartOnboardingWithSSNCommand;
import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.query.MemberRepository;
import com.hedvig.memberservice.services.member.MemberQueryService;
import com.hedvig.memberservice.services.trace.TraceMemberService;
import com.hedvig.memberservice.web.InternalMembersController;
import com.hedvig.memberservice.web.dto.Address;
import com.hedvig.memberservice.web.dto.InternalMemberSearchRequestDTO;
import com.hedvig.memberservice.web.dto.InternalMemberSearchResultDTO;
import com.hedvig.memberservice.web.dto.MemberFraudulentStatusDTO;
import com.hedvig.memberservice.web.dto.StartOnboardingWithSSNRequest;
import com.hedvig.memberservice.web.dto.UpdateContactInformationRequest;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.spring.config.EnableAxon;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestApplication.class)
@WebMvcTest(controllers = InternalMembersController.class)
@EnableAxon
public class InternalMembersControllerTests {
  @Autowired private MockMvc mockMvc;

  @MockBean private MemberRepository memberRepo;

  @MockBean private MemberQueryService memberQueryService;

  @MockBean private CommandGateway commandGateway;

  @MockBean private TraceMemberService traceMemberService;

  @Test
  public void TestUpdateContactInformation() throws Exception {
    UpdateContactInformationRequest request = new UpdateContactInformationRequest();
    request.setFirstName("Mr X");
    request.setLastName("X:son");
    request.setMemberId("1337");
    request.setAddress(new Address());
    request.getAddress().setZipCode("12345");
    request.getAddress().setStreet("Gatan 57");
    request.getAddress().setCity("Småstaden");
    request.getAddress().setApartmentNo("1203");

    ObjectMapper jsonMapper = new ObjectMapper();

    mockMvc
        .perform(
            post("/i/member/{memberId}/finalizeOnboarding", "1337")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(jsonMapper.writeValueAsBytes(request)))
        .andExpect(status().is2xxSuccessful());

    verify(commandGateway, times(1))
        .sendAndWait(new MemberUpdateContactInformationCommand(1337l, request));
  }

  @Test
  public void TestStartOnboardingWithSSN() throws Exception {
    final String ssn = "190304059999";
    StartOnboardingWithSSNRequest request = new StartOnboardingWithSSNRequest(ssn);

    when(memberRepo.findBySsn(ssn)).thenReturn(Optional.empty());

    ObjectMapper jsonMapper = new ObjectMapper();

    mockMvc
        .perform(
            post("/i/member/{memberId}/startOnboardingWithSSN", "1337")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(jsonMapper.writeValueAsBytes(request)))
        .andExpect(status().is2xxSuccessful());

    verify(commandGateway, times(1)).sendAndWait(new StartOnboardingWithSSNCommand(1337l, request));
  }

  @Test
  public void TestStartOnboardingWithSSN_AlreadyMember() throws Exception {
    final String ssn = "190304059999";
    final long memberId = 1337l;

    MemberEntity member = new MemberEntity();
    member.setId(memberId);

    when(memberRepo.findBySsn(ssn)).thenReturn(Optional.of(member));

    StartOnboardingWithSSNRequest request = new StartOnboardingWithSSNRequest(ssn);
    ObjectMapper jsonMapper = new ObjectMapper();

    mockMvc
        .perform(
            post("/i/member/{memberId}/startOnboardingWithSSN", Objects.toString(memberId))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(jsonMapper.writeValueAsBytes(request)))
        .andExpect(status().isNoContent());

    verify(commandGateway, times(1))
        .sendAndWait(new StartOnboardingWithSSNCommand(memberId, request));
  }

  @Test
  public void TestSearchAcceptsEmptyStatus() throws Exception {
    InternalMemberSearchRequestDTO request = new InternalMemberSearchRequestDTO("", null, null, null, null, null);
    when(memberQueryService.search(request)).thenReturn(new InternalMemberSearchResultDTO(Collections.emptyList(), null, null));

    mockMvc
      .perform(
        get("/i/member/search?status=&query=")
      )
      .andExpect(status().is2xxSuccessful());

    verify(memberQueryService, times(1))
      .search(request);
  }

  @Test
  public void testFraudulentStatus() throws Exception {
    final MemberFraudulentStatusDTO fraudulentStatus = new MemberFraudulentStatusDTO("SUSPECTED_FRAUD", "Just for testing");
    final MemberFraudulentStatusDTO emptyFraudulentStatus = new MemberFraudulentStatusDTO(null, "Just for testing");
    final Long memberId = 1337L;
    final String token = "12345";

    mockMvc
      .perform(
        post("/_/member/{memberId}/setFraudulentStatus", memberId)
          .contentType(MediaType.APPLICATION_JSON_UTF8)
          .content(new ObjectMapper().writeValueAsBytes(fraudulentStatus)).header("Authorization", token))
      .andExpect(status().is2xxSuccessful());

    verify(commandGateway)
      .sendAndWait(
        new SetFraudulentStatusCommand(
          memberId,
          FraudulentStatus.SUSPECTED_FRAUD.toString(),
          fraudulentStatus.getFraudulentStatusDescription(),
          token)
      );

    mockMvc
      .perform(
        post("/_/member/{memberId}/setFraudulentStatus", memberId)
          .contentType(MediaType.APPLICATION_JSON_UTF8)
          .content(new ObjectMapper().writeValueAsBytes(emptyFraudulentStatus)).header("Authorization", token))
      .andExpect(status().is2xxSuccessful());

    verify(commandGateway)
      .sendAndWait(
        new SetFraudulentStatusCommand(
          memberId,
          FraudulentStatus.NOT_FRAUD.toString(),
          emptyFraudulentStatus.getFraudulentStatusDescription(),
          token)
      );
  }
}
