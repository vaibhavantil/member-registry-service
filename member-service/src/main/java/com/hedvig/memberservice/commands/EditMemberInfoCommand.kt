package com.hedvig.memberservice.commands

import com.hedvig.memberservice.web.dto.EditMemberInfoRequest
import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.lang.Long.parseLong

class EditMemberInfoCommand(
  @TargetAggregateIdentifier
  val memberId: Long,
  val firstName: String?,
  val lastName: String?,
  val email: String?,
  val phoneNumber: String?,
  val token: String
) {
  companion object {
    fun from(request: EditMemberInfoRequest, token: String) = EditMemberInfoCommand(
      memberId = parseLong(request.memberId),
      firstName = request.firstName,
      lastName = request.lastName,
      email = request.email,
      phoneNumber = request.phoneNumber,
      token = token
    )
  }
}
