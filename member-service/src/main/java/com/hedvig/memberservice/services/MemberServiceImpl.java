package com.hedvig.memberservice.services;

import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.query.MemberRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl implements MemberService {

  @Autowired MemberRepository memberRepository;

  public Optional<MemberEntity> findMemberById(Long memberId) {
    return memberRepository.findById(memberId);
  }

  public Page<MemberEntity> search(String status, String query, Pageable pageable) {

    if (!query.equals("")) {
      Long memberId = parseMemberId(query);
      if (memberId != null) {
        if (!status.equals("")) {
          return memberRepository.searchByIdAndStatus(memberId, status, pageable);
        } else {
          return memberRepository.searchById(memberId, pageable);
        }
      }
    }

    if (!status.equals("") && !query.equals("")) {
      return memberRepository.searchByStatusAndQuery(status, query, pageable);
    }
    if (!status.equals("")) {
      return memberRepository.searchByStatus(status, pageable);
    }
    if (!query.equals("")) {
      return memberRepository.searchByQuery(query, pageable);
    }
    return this.listAllMembers(pageable);
  }

  public Page<MemberEntity> listAllMembers(Pageable pageable) {
    return memberRepository.findAll(pageable);
  }

  private Long parseMemberId(String query) {
    try {
      return Long.parseLong(query);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
