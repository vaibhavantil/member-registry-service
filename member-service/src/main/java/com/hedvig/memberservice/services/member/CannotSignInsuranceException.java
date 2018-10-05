package com.hedvig.memberservice.services.member;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value= HttpStatus.NOT_ACCEPTABLE, reason = "Insurance cannot be signed")
public class CannotSignInsuranceException extends RuntimeException {

}
