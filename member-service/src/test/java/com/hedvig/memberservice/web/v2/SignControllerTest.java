package com.hedvig.memberservice.web.v2;

import com.hedvig.external.bankID.bankIdTypes.CollectStatus;
import com.hedvig.external.bankID.bankIdTypes.OrderResponse;
import com.hedvig.memberservice.entities.CollectResponse;
import com.hedvig.memberservice.entities.SignSession;
import com.hedvig.memberservice.entities.SignStatus;
import com.hedvig.memberservice.services.BankIdRestService;
import com.hedvig.memberservice.services.MemberHasExistingInsuranceException;
import com.hedvig.memberservice.services.SigningService;
import com.hedvig.memberservice.services.member.dto.MemberSignResponse;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = SignController.class)
@ActiveProfiles(profiles = "unittest")
public class SignControllerTest {

  private static final String ORDER_REFERENCE = "orderReference";
  private static final String AUTOSTART_TOKEN = "autostartToken";
  private static final long MEMBER_ID = 1337L;

  @Autowired MockMvc mockMvc;

  @MockBean
  SigningService signingService;

  @MockBean
  BankIdRestService bankIdService;



  @Test
  public void postToWebsign_givenMemberWithExistingInsurance_thenReturn403() throws Exception {

    given(signingService.startWebSign(anyLong(), any())).willThrow(MemberHasExistingInsuranceException.class);

    mockMvc
        .perform(
            post("/v2/member/sign/websign")
                .header("hedvig.token", "1337")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .content("{\"email\":\"test@test.com\", \"ssn\": \"191212121212\", \"ipAddress\":\"127.0.0.1\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  public void postToWebsign_givenMemberWithOkProduct_willReturn200() throws Exception {

    given(signingService.startWebSign(anyLong(), any()))
        .willReturn(new MemberSignResponse(10L,
            SignStatus.IN_PROGRESS, new OrderResponse(ORDER_REFERENCE, AUTOSTART_TOKEN)));

    mockMvc
        .perform(
            post("/v2/member/sign/websign")
                .header("hedvig.token", "1337")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .content("{\"email\":\"test@test.com\", \"ssn\": \"191212121212\", \"ipAddress\":\"127.0.0.1\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath(".bankIdOrderResponse.orderRef").exists())
        .andExpect(jsonPath(".bankIdOrderResponse.autoStartToken").exists())
        .andExpect(jsonPath(".status").value("IN_PROGRESS"))
        .andExpect(jsonPath(".signId").value(10));
  }

  @Test
  public void getSignStatus_givenNoActiveSession_thenReturn404() throws Exception{

    given(signingService.getSignStatus(MEMBER_ID)).willReturn(Optional.empty());

    mockMvc
        .perform(
            get("/v2/member/sign/signStatus")
                .header("hedvig.token", "1337")
                .header("Accept", "application/json"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void getSignStatus_givenNoCollectResponse_thenReturnOkWithCollectDataNull() throws Exception {

    SignSession session = makeSignSession(AUTOSTART_TOKEN);

    given(signingService.getSignStatus(MEMBER_ID)).willReturn(Optional.of(session));

    mockMvc
        .perform(
            get("/v2/member/sign/signStatus")
                .header("hedvig.token", "1337")
                .header("Accept", "application/json"))
        .andExpect(status().isOk())
        .andExpect(jsonPath(".collectData").value(allOf(contains(nullValue()),hasSize(1))));
  }

  @Test
  public void getSignStatus_givenCollectResponse_thenReturnCollectResponse() throws Exception{

    SignSession session = makeSignSession(AUTOSTART_TOKEN);

    val cr = new CollectResponse();
    cr.setHintCode(null);
    cr.setStatus(CollectStatus.complete);

    session.newCollectResponse(cr);

    given(signingService.getSignStatus(MEMBER_ID)).willReturn(Optional.of(session));

    mockMvc
        .perform(
            get("/v2/member/sign/signStatus")
                .header("hedvig.token", "1337")
                .header("Accept", "application/json"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("collectData.status").value("complete"))
        .andExpect(jsonPath("status").value( "IN_PROGRESS"));
  }

  public SignSession makeSignSession(String autostartToken) {
    val session = new SignSession(MEMBER_ID);
    session.newOrderStarted(new OrderResponse(ORDER_REFERENCE, autostartToken));
    return session;
  }
}
