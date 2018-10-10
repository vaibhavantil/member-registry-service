package com.hedvig.memberservice.services;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value= HttpStatus.FORBIDDEN, reason = "Insurance already exists for person")
public class MemberHasExistingInsuranceException extends RuntimeException {



}
