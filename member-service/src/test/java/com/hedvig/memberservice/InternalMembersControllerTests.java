package com.hedvig.memberservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.memberservice.aggregates.FraudulentStatus;
import com.hedvig.memberservice.commands.MemberUpdateContactInformationCommand;
import com.hedvig.memberservice.commands.SetFraudulentStatusCommand;
import com.hedvig.memberservice.commands.StartSwedishOnboardingWithSSNCommand;
import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.query.MemberRepository;
import com.hedvig.memberservice.services.member.MemberQueryService;
import com.hedvig.memberservice.services.trace.TraceMemberService;
import com.hedvig.memberservice.web.InternalMembersController;
import com.hedvig.memberservice.web.dto.*;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.spring.config.EnableAxon;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
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
    Address address =  new Address();

    address.setZipCode("12345");
    address.setStreet("Gatan 57");
    address.setCity("Småstaden");
    address.setApartmentNo("1203");

    UpdateContactInformationRequest request = new UpdateContactInformationRequest(
        "1337",
        "Mr X",
        "X:son",
        null,
        null,
        address,
        null
    );

    ObjectMapper jsonMapper = new ObjectMapper();

    mockMvc
        .perform(
            post("/i/member/{memberId}/finalizeOnboarding", "1337")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(jsonMapper.writeValueAsBytes(request)))
        .andExpect(status().is2xxSuccessful());

    ArgumentCaptor<MemberUpdateContactInformationCommand> argumentCaptor = ArgumentCaptor.forClass(MemberUpdateContactInformationCommand.class);

    verify(commandGateway)
        .sendAndWait(argumentCaptor.capture());

    assertThat(argumentCaptor.getValue().getMemberId()).isEqualTo(1337L);
    assertThat(argumentCaptor.getValue().getFirstName()).isEqualTo("Mr X");
    assertThat(argumentCaptor.getValue().getLastName()).isEqualTo("X:son");

    assertThat(argumentCaptor.getValue().getZipCode()).isEqualTo("12345");
    assertThat(argumentCaptor.getValue().getStreet()).isEqualTo("Gatan 57");
    assertThat(argumentCaptor.getValue().getCity()).isEqualTo("Småstaden");
    assertThat(argumentCaptor.getValue().getApartmentNo()).isEqualTo("1203");
  }

  @Test
  public void TestStartOnboardingWithSSN() throws Exception {
    final String ssn = "190304059999";
    StartOnboardingWithSSNRequest request = new StartOnboardingWithSSNRequest(ssn);

    when(memberRepo.findBySsn(ssn)).thenReturn(Collections.emptyList());

    ObjectMapper jsonMapper = new ObjectMapper();

    mockMvc
        .perform(
            post("/i/member/{memberId}/startOnboardingWithSSN", "1337")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(jsonMapper.writeValueAsBytes(request)))
        .andExpect(status().is2xxSuccessful());

    ArgumentCaptor<StartSwedishOnboardingWithSSNCommand> argumentCaptor = ArgumentCaptor.forClass(StartSwedishOnboardingWithSSNCommand.class);

    verify(commandGateway, times(1))
        .sendAndWait(argumentCaptor.capture());

    assertThat(argumentCaptor.getValue().getSsn()).isEqualTo(ssn);
    assertThat(argumentCaptor.getValue().getMemberId()).isEqualTo(1337L);
  }

  @Test
  public void TestStartOnboardingWithSSN_AlreadyMember() throws Exception {
    final String ssn = "190304059999";
    final long memberId = 1337l;

    MemberEntity member = new MemberEntity();
    member.setId(memberId);

    when(memberRepo.findBySsn(ssn)).thenReturn(Collections.emptyList());

    StartOnboardingWithSSNRequest request = new StartOnboardingWithSSNRequest(ssn);
    ObjectMapper jsonMapper = new ObjectMapper();

    mockMvc
        .perform(
            post("/i/member/{memberId}/startOnboardingWithSSN", Objects.toString(memberId))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(jsonMapper.writeValueAsBytes(request)))
        .andExpect(status().isNoContent());


    ArgumentCaptor<StartSwedishOnboardingWithSSNCommand> argumentCaptor = ArgumentCaptor.forClass(StartSwedishOnboardingWithSSNCommand.class);

    verify(commandGateway, times(1))
        .sendAndWait(argumentCaptor.capture());

    assertThat(argumentCaptor.getValue().getSsn()).isEqualTo(ssn);
    assertThat(argumentCaptor.getValue().getMemberId()).isEqualTo(memberId);
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
    final Long memberId = 1337L;
    final String token = "12345";

    mockMvc
      .perform(
        post("/_/member/{memberId}/setFraudulentStatus", memberId)
          .contentType(MediaType.APPLICATION_JSON_UTF8)
          .content(new ObjectMapper().writeValueAsBytes(fraudulentStatus)).header("Authorization", token))
      .andExpect(status().is2xxSuccessful());

    ArgumentCaptor<SetFraudulentStatusCommand> argumentCaptor = ArgumentCaptor.forClass(SetFraudulentStatusCommand.class);

    verify(commandGateway)
        .sendAndWait(argumentCaptor.capture());

    assertThat(argumentCaptor.getValue().getFraudulentStatus()).isEqualTo(FraudulentStatus.SUSPECTED_FRAUD);
    assertThat(argumentCaptor.getValue().getMemberId()).isEqualTo(1337L);
    assertThat(argumentCaptor.getValue().getFraudulentDescription()).isEqualTo(fraudulentStatus.getFraudulentStatusDescription());
    assertThat(argumentCaptor.getValue().getToken()).isEqualTo(token);

  }

    @Test
    public void testEmptyFraudulentStatus() throws Exception {
      final MemberFraudulentStatusDTO emptyFraudulentStatus = new MemberFraudulentStatusDTO(null, "Just for testing");
      final Long memberId = 1337L;
      final String token = "12345";

      mockMvc
          .perform(
              post("/_/member/{memberId}/setFraudulentStatus", memberId)
                  .contentType(MediaType.APPLICATION_JSON_UTF8)
                  .content(new ObjectMapper().writeValueAsBytes(emptyFraudulentStatus)).header("Authorization", token))
          .andExpect(status().is2xxSuccessful());

      ArgumentCaptor<SetFraudulentStatusCommand> argumentCaptor2 = ArgumentCaptor.forClass(SetFraudulentStatusCommand.class);

      verify(commandGateway)
          .sendAndWait(argumentCaptor2.capture());

      assertThat(argumentCaptor2.getValue().getFraudulentStatus()).isEqualTo(FraudulentStatus.NOT_FRAUD);
      assertThat(argumentCaptor2.getValue().getMemberId()).isEqualTo(1337L);
      assertThat(argumentCaptor2.getValue().getFraudulentDescription()).isEqualTo(emptyFraudulentStatus.getFraudulentStatusDescription());
      assertThat(argumentCaptor2.getValue().getToken()).isEqualTo(token);
    }
}
