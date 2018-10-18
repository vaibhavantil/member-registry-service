package com.hedvig.memberservice.web.dto;

import com.hedvig.memberservice.aggregates.MemberStatus;
import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
public class InternalMemberSearchRequestDTO {
  String query;
  MemberStatus status;
  Integer page;
  Integer pageSize;
  MembersSortColumn sortBy;
  Sort.Direction sortDirection;
}
