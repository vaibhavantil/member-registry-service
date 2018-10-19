package com.hedvig.memberservice.web.dto;

import com.hedvig.memberservice.aggregates.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InternalMemberSearchRequestDTO {
  String query;
  MemberStatus status;
  Integer page;
  Integer pageSize;
  MembersSortColumn sortBy;
  Sort.Direction sortDirection;
}
