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
      pageableBuilder = pageableBuilder.orderBy(entSortProp, request.getSortDirection(), Sort.NullHandling.NULLS_FIRST);
    }

    Pageable pageReq = pageableBuilder.build();

    if (StringUtils.isEmpty(request.getQuery())) {
      return Page.empty();
    }

    if (request.getIncludeAll() != null && request.getIncludeAll()) {
      return memberRepository.searchAll(request.getQuery().trim(), pageReq);
    } else {
      return memberRepository.searchSignedOrTerminated(request.getQuery().trim(), pageReq);
    }
  }
}
