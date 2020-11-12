package com.hedvig.memberservice;

import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.services.cashback.CashbackService;
import com.hedvig.memberservice.web.CashbackController;
import com.hedvig.memberservice.web.dto.CashbackOption;
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
public class CashbackControllerTests {
  @Autowired private MockMvc mockMvc;

  @MockBean
  CashbackService cashbackService;

  private CashbackOption createCashbackOption(UUID newOptionId) {
    return new CashbackOption(newOptionId, "", "", "", false, true, "", "", "", "");
  }

  @Test
  public void PostCashbackOption() throws Exception {
    final long memberId = 1337L;

    MemberEntity member = new MemberEntity();
    member.setId(memberId);
    final UUID newOptionId = UUID.fromString("d24c427e-d110-11e7-a47e-0b4e39412e98");

    CashbackOption cashbackOption = createCashbackOption(newOptionId);

    when(cashbackService.getMembersCashbackOption(memberId)).thenReturn(Optional.of(cashbackOption));

    mockMvc
        .perform(
            post("/cashback", "")
                .param("optionId", newOptionId.toString())
                .header("hedvig.token", memberId))
        .andExpect(status().isNoContent());

    verify(cashbackService, times(1))
      .selectCashbackOption(memberId, newOptionId);
  }
}
