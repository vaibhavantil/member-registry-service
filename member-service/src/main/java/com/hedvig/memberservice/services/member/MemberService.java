package com.hedvig.memberservice.services.member;

import com.hedvig.external.bankID.bankIdRestTypes.CollectResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

  public void bankIdSignComplete(final long memberId, @NonNull final CollectResponse collectResponse) {

  }
}
