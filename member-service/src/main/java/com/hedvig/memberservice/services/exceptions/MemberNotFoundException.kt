package com.hedvig.memberservice.services.exceptions

import java.lang.RuntimeException

class MemberNotFoundException(memberId: Long) : RuntimeException(
    "Could not find a member with id $memberId"
)
