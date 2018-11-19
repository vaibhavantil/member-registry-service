package com.hedvig.memberservice.web.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InternalMemberSearchResultDTO {
  List<InternalMember> members;
  Integer page;
  Integer totalPages;
}
