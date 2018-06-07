package com.hedvig.memberservice.services;

import com.hedvig.memberservice.query.MemberEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberService {

  Page<MemberEntity> listAllMembers(Pageable pageable);

  Page<MemberEntity> search(String status, String query, Pageable pageable);

  Optional<MemberEntity> findMemberById(Long memberId);

}
