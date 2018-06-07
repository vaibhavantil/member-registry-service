package com.hedvig.memberservice.services;

import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.query.MemberRepository;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

  private static final int PAGESIZE = 25;

  @Autowired MemberRepository memberRepository;

  public Optional<MemberEntity> findMemberById(Long memberId) {
    return memberRepository.findById(memberId);
  }

  public Stream<MemberEntity> search(String status, String query) {

    if (!query.equals("")) {
      Long memberId = parseMemberId(query);
      if (memberId != null) {
        if (!status.equals("")) {
          return memberRepository.searchByIdAndStatus(memberId, status);
        } else {
          return memberRepository.searchById(memberId);
        }
      }
    }

    if (!status.equals("") && !query.equals("")) {
      return memberRepository.searchByStatusAndQuery(status, query);
    }
    if (!status.equals("")) {
      return memberRepository.searchByStatus(status);
    }
    if (!query.equals("")) {
      return memberRepository.searchByQuery(query);
    }
    return memberRepository.searchAll();

  }

  private Long parseMemberId(String query) {
    try {
      return Long.parseLong(query);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
