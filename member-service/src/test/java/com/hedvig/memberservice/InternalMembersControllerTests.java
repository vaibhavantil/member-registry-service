package com.hedvig.memberservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.memberservice.commands.MemberUpdateContactInformationCommand;
import com.hedvig.memberservice.commands.StartOnboardingWithSSNCommand;
import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.query.MemberRepository;
import com.hedvig.memberservice.web.InternalMembersController;
import com.hedvig.memberservice.web.dto.Address;
import com.hedvig.memberservice.web.dto.StartOnboardingWithSSNRequest;
import com.hedvig.memberservice.web.dto.UpdateContactInformationRequest;
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

import java.util.Objects;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestApplication.class)
@WebMvcTest(controllers = InternalMembersController.class)
@EnableAxon
public class InternalMembersControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberRepository memberRepo;

    @MockBean
    private CommandGateway commandGateway;

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

        mockMvc.perform(
                post("/i/member/{memberId}/finalizeOnboarding", "1337").
                contentType(MediaType.APPLICATION_JSON_UTF8).
                content(jsonMapper.writeValueAsBytes(request))).andExpect(status().is2xxSuccessful());

        verify(commandGateway, times(1)).sendAndWait(new MemberUpdateContactInformationCommand(1337l, request));
    }

    @Test
    public void TestStartOnboardingWithSSN() throws Exception {
        final String ssn = "190304059999";
        StartOnboardingWithSSNRequest request = new StartOnboardingWithSSNRequest(ssn);

        when(memberRepo.findBySsn(ssn)).thenReturn(Optional.empty());

        ObjectMapper jsonMapper = new ObjectMapper();

        mockMvc.perform(
                post("/i/member/{memberId}/startOnboardingWithSSN", "1337").
                        contentType(MediaType.APPLICATION_JSON_UTF8).
                        content(jsonMapper.writeValueAsBytes(request))).andExpect(status().is2xxSuccessful());

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

        mockMvc.perform(
                post("/i/member/{memberId}/startOnboardingWithSSN", Objects.toString(memberId)).
                        contentType(MediaType.APPLICATION_JSON_UTF8).
                        content(jsonMapper.writeValueAsBytes(request))).andExpect(status().isAlreadyReported());

        verify(commandGateway, times(0)).sendAndWait(new StartOnboardingWithSSNCommand(memberId, request));
    }

}
