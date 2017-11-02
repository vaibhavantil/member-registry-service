package com.hedvig.memberservice.web.dto;

import lombok.Data;

@Data
public class FinalizeOnBoardingRequest {

    String memberId;

    String ssn;
    String firstName;
    String lastName;
    String email;

    Address address;

}
