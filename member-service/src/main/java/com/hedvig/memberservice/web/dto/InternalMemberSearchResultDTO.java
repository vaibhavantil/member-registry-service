package com.hedvig.memberservice.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class InternalMemberSearchResultDTO {
  List<InternalMember> members;
  Integer page;
  Integer totalPages;
}
