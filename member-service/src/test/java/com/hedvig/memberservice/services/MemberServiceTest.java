package com.hedvig.memberservice.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.BDDMockito.given;

import com.hedvig.external.bankID.bankIdRestTypes.BankIdRestError;
import com.hedvig.external.bankID.bankIdRestTypes.OrderResponse;
import com.hedvig.memberservice.externalApi.productsPricing.ProductApi;
import com.hedvig.memberservice.query.SignedMemberEntity;
import com.hedvig.memberservice.query.SignedMemberRepository;
import com.hedvig.memberservice.services.member.CannotSignInsuranceException;
import com.hedvig.memberservice.web.v2.dto.WebsignRequest;
import java.util.Optional;
import lombok.val;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MemberServiceTest {

  @Mock
  ProductApi productApi;

  @Mock
  BankIdRestService bankIdRestService;

  @Mock
  SignedMemberRepository signedMemberRepository;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setup(){
    given(signedMemberRepository.findBySsn(any())).willReturn(Optional.empty());
  }


  @Test
  public void startWebSign_givenMemberWithOkProduct_thenReturnOrderRefAndAutoStartToken(){

    val memberId = 1337L;
    val ssn = "191212121212";

    given(productApi.hasProductToSign(memberId)).willReturn(true);
    given(bankIdRestService.startSign(eq(memberId), matches(ssn), anyString())).willReturn(new OrderResponse("orderRef", "autostartToken"));


    val sut = new MemberService(bankIdRestService, productApi, signedMemberRepository);
    val result = sut.startWebSign(memberId, new WebsignRequest("test@test.com", ssn));

    assertThat(result.getBankIdOrderResponse()).hasFieldOrProperty("orderRef");
    assertThat(result.getBankIdOrderResponse()).hasFieldOrProperty("autoStartToken");

  }

  @Test
  public void startWebSign_givenMemberWithoutOkProduct_thenThrowException(){

    val memberId = 1337L;
    val ssn = "191212121212";

    given(productApi.hasProductToSign(memberId)).willReturn(false);
    val sut = new MemberService(bankIdRestService, productApi, signedMemberRepository);

    thrown.expect(CannotSignInsuranceException.class);
    sut.startWebSign(memberId, new WebsignRequest("test@test.com", ssn));
  }

  @Test
  public void startWebSign_givenMemberInSignedMemberEntity_thenThrowException(){

    val memberId = 1337L;
    val ssn = "191212121212";

    val memberEntity = new SignedMemberEntity();
    memberEntity.setId(memberId);
    memberEntity.setSsn(ssn);
    given(signedMemberRepository.findBySsn(ssn)).willReturn(Optional.of(memberEntity));
    val sut = new MemberService(bankIdRestService, productApi, signedMemberRepository );

    thrown.expect(MemberHasExistingInsuranceException.class);
    sut.startWebSign(memberId, new WebsignRequest("test@test.com", ssn));
  }

  @Test
  public void startWebSign_givenBankidThrowsError_thenThrowException(){
    val memberId = 1337L;
    val ssn = "191212121212";

    given(productApi.hasProductToSign(memberId)).willReturn(true);
    given(bankIdRestService.startSign(anyLong(), any(), any())).willThrow(BankIdRestError.class);

    val sut = new MemberService(bankIdRestService, productApi, signedMemberRepository );

    thrown.expect(BankIdRestError.class);
    sut.startWebSign(memberId, new WebsignRequest("test@test.com", ssn));

  }

}