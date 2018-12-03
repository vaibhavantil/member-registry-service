package com.hedvig.memberservice.services.trace;

import com.hedvig.memberservice.web.dto.TraceMemberDTO;

import java.util.Collection;

public interface TraceMemberService {
  Collection<TraceMemberDTO> getTracesByMemberId(Long memberId);
}
