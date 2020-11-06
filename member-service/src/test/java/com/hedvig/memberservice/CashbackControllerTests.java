package com.hedvig.memberservice;

import com.hedvig.memberservice.aggregates.PickedLocale;
import com.hedvig.memberservice.commands.SelectNewCashbackCommand;
import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.query.MemberRepository;
import com.hedvig.memberservice.services.cashback.CashbackService;
import com.hedvig.memberservice.web.CashbackController;
import com.hedvig.memberservice.web.dto.CashbackOption;
import com.hedvig.memberservice.web.dto.StartOnboardingWithSSNRequest;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.spring.config.EnableAxon;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = CashbackController.class)
@EnableAxon
public class CashbackControllerTests {
  @Autowired private MockMvc mockMvc;

  @MockBean MemberRepository memberRepo;

  @MockBean CommandGateway commandGateway;

  @MockBean
  CashbackService cashbackService;

  private CashbackOption createCashbackOption(UUID newOptionId) {
    return new CashbackOption(newOptionId, "", "", "", false, true, "", "", "", "");
  }

  @Test
  public void PostCashbackOption() throws Exception {
    final long memberId = 1337L;

    PickedLocale pickedLocale = PickedLocale.sv_SE;
    MemberEntity member = new MemberEntity();
    member.setId(memberId);
    member.setPickedLocale(pickedLocale);
    final UUID newOptionId = UUID.fromString("d24c427e-d110-11e7-a47e-0b4e39412e98");

    CashbackOption cashbackOption = createCashbackOption(newOptionId);

    when(cashbackService.getCashbackOption(newOptionId, pickedLocale)).thenReturn(Optional.of(cashbackOption));
    when(memberRepo.findById(memberId)).thenReturn(Optional.of(member));

    mockMvc
        .perform(
            post("/cashback", "")
                .param("optionId", newOptionId.toString())
                .header("hedvig.token", memberId))
        .andExpect(status().isNoContent());

    verify(commandGateway, times(1))
        .sendAndWait(new SelectNewCashbackCommand(memberId, newOptionId));
  }

  @Test
  public void PostCashbackOption_WHEN_OptionId_IsnotFound() throws Exception {
    final long memberId = 1337l;

    PickedLocale pickedLocale = PickedLocale.sv_SE;
    final UUID newOptionId = UUID.fromString("d24c427e-d110-11e7-a47e-0b4e39412e99");

    MemberEntity member = new MemberEntity();
    member.setId(memberId);
    member.setPickedLocale(pickedLocale);

    when(cashbackService.getCashbackOption(newOptionId, pickedLocale)).thenReturn(Optional.empty());
    when(memberRepo.findById(memberId)).thenReturn(Optional.of(member));

    mockMvc
        .perform(
            post("/cashback", "")
                .param("optionId", newOptionId.toString())
                .header("hedvig.token", memberId))
        .andExpect(status().isNotFound());

    verify(commandGateway, times(0))
        .sendAndWait(new SelectNewCashbackCommand(memberId, newOptionId));
  }

  @Test
  public void PostCashbackOption_WHEN_member_IsNotFound() throws Exception {
    final long memberId = 1337l;
    final UUID newOptionId = UUID.fromString("d24c427e-d110-11e7-a47e-0b4e39412e98");

    final CashbackOption cashbackOption = createCashbackOption(newOptionId);

    PickedLocale pickedLocale = PickedLocale.sv_SE;
    MemberEntity member = new MemberEntity();
    member.setId(memberId);
    member.setPickedLocale(pickedLocale);

    when(cashbackService.getCashbackOption(newOptionId, pickedLocale)).thenReturn(Optional.of(cashbackOption));
    when(memberRepo.findById(memberId)).thenReturn(Optional.empty());

    new StartOnboardingWithSSNRequest("");

    mockMvc
        .perform(
            post("/cashback", "")
                .param("optionId", newOptionId.toString())
                .header("hedvig.token", memberId))
        .andExpect(status().isNotFound());

    verify(commandGateway, times(0))
        .sendAndWait(new SelectNewCashbackCommand(memberId, newOptionId));
  }
}
