package com.hedvig.memberservice.services;

import com.hedvig.memberservice.externalApi.productsPricing.ProductApi;
import com.hedvig.memberservice.query.SignedMemberRepository;
import com.hedvig.memberservice.services.member.CannotSignInsuranceException;
import com.hedvig.memberservice.services.member.dto.MemberSignResponse;
import com.hedvig.memberservice.web.v2.dto.WebsignRequest;
import javax.transaction.Transactional;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

  private final BankIdRestService bankidService;
  private final ProductApi productApi;
  private final SignedMemberRepository signedMemberRepository;

  public MemberService(
      BankIdRestService bankidService,
      ProductApi productApi,
      SignedMemberRepository signedMemberRepository) {
    this.bankidService = bankidService;
    this.productApi = productApi;
    this.signedMemberRepository = signedMemberRepository;
  }

  @Transactional
  public MemberSignResponse startWebSign(final long memberId, final WebsignRequest request) {

    val existing = signedMemberRepository.findBySsn(request.getSsn());

    if (existing.isPresent()) {
      throw new MemberHasExistingInsuranceException();
    }

    if (productApi.hasProductToSign(memberId)) {
      val result = bankidService.startSign(memberId, request.getSsn(), "SomeMessage");
      return new MemberSignResponse(result);
    } else {
      throw new CannotSignInsuranceException();
    }
  }
}