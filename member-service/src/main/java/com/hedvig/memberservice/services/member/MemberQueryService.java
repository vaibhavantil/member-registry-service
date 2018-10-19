package com.hedvig.memberservice.services.member;

import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.query.MemberRepository;
import com.hedvig.memberservice.util.PageableBuilder;
import com.hedvig.memberservice.web.dto.InternalMember;
import com.hedvig.memberservice.web.dto.InternalMemberSearchRequestDTO;
import com.hedvig.memberservice.web.dto.InternalMemberSearchResultDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberQueryService {
  private final MemberRepository memberRepository;

  public MemberQueryService(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
  }

  @Transactional
  public InternalMemberSearchResultDTO search(InternalMemberSearchRequestDTO searchReq) {
    Page<MemberEntity> searchRes = searchInternal(searchReq);

    List<InternalMember> memberDTOs = searchRes.getContent().stream()
      .map(InternalMember::fromEntity)
      .collect(Collectors.toList());

    if (searchRes.getPageable().isPaged()) {
      return new InternalMemberSearchResultDTO(memberDTOs, searchReq.getPage(), searchRes.getTotalPages());
    } else {
      return new InternalMemberSearchResultDTO(memberDTOs, null, null);
    }
  }

  private Page<MemberEntity> searchInternal(InternalMemberSearchRequestDTO request) {
    PageableBuilder pageableBuilder = new PageableBuilder();

    if (request.getPage() != null && request.getPageSize() != null) {
      pageableBuilder = pageableBuilder.paged(request.getPage(), request.getPageSize());
    }

    if (request.getSortBy() != null) {
      String entSortProp = MemberEntity.SORT_COLUMN_MAPPING.get(request.getSortBy());
      pageableBuilder = pageableBuilder.orderBy(entSortProp, request.getSortDirection(), Sort.NullHandling.NULLS_LAST);
    }

    Pageable pageReq = pageableBuilder.build();


    boolean haveQuery = StringUtils.hasText(request.getQuery());
    boolean haveStatus = request.getStatus() != null;
    if (haveQuery) {
      Long memberId = parseMemberId(request.getQuery().trim());
      if (memberId != null) {
        if (haveStatus) {
          return memberRepository.searchByIdAndStatus(memberId, request.getStatus(), pageReq);
        } else {
          return memberRepository.searchById(memberId, pageReq);
        }
      }
    }

    if (haveStatus && haveQuery) {
      return memberRepository.searchByStatusAndQuery(request.getStatus(), request.getQuery().trim(), pageReq);
    }
    if (haveStatus) {
      return memberRepository.searchByStatus(request.getStatus(), pageReq);
    }
    if (haveQuery) {
      return memberRepository.searchByQuery(request.getQuery().trim(), pageReq);
    }

    return memberRepository.searchAll(pageReq);
  }

  private Long parseMemberId(String query) {
    try {
      return Long.parseLong(query);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
