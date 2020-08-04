package com.hedvig.memberservice.web.dto

data class EditMemberInfoRequest(
  val memberId: String,
  val firstName: String?,
  val lastName: String?,
  val email: String?,
  val phoneNumber: String?
)
