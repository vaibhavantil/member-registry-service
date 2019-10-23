package com.hedvig.memberservice.commands

data class CreateMemberCommand(
  val memberId: Long,
  val attributionCode: String? = null
)
