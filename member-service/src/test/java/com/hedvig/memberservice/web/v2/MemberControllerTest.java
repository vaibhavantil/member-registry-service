package com.hedvig.memberservice.web.v2;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hedvig.external.bankID.bankIdRestTypes.OrderResponse;
import com.hedvig.memberservice.TestApplication;
import com.hedvig.memberservice.services.MemberHasExistingInsuranceException;
import com.hedvig.memberservice.services.MemberService;
import com.hedvig.memberservice.services.member.dto.MemberSignResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestApplication.class)
@WebMvcTest(controllers = MemberController.class)
@ActiveProfiles(profiles = "unittest")
public class MemberControllerTest {

  @Autowired MockMvc mockMvc;

  @MockBean MemberService memberService;

  @Test
  public void postToWebsign_givenMemberWithExistingInsurance_thenReturn403() throws Exception {

    given(memberService.startWebSign(anyLong(), any())).willThrow(MemberHasExistingInsuranceException.class);

    mockMvc
        .perform(
            post("/v2/member/websign")
                .header("hedvig.token", "1337")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .content("{\"email\":\"test@test.com\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  public void postToWebsign_givenMemberWithOkProduct_willReturn200() throws Exception {

    given(memberService.startWebSign(anyLong(), any()))
        .willReturn(new MemberSignResponse(new OrderResponse("orderRef", "autostartToken")));

    mockMvc
        .perform(
            post("/v2/member/websign")
                .header("hedvig.token", "1337")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .content("{\"email\":\"test@test.com\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath(".bankIdOrderResponse.orderRef").exists());
  }
}
