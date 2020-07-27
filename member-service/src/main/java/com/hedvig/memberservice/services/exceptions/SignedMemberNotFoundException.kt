package com.hedvig.memberservice.services.exceptions

import java.lang.RuntimeException

class SignedMemberNotFoundException(ssn: String, repositoryName: String) : RuntimeException(
    "Could not find a signed member with SSN $ssn in $repositoryName"
)
