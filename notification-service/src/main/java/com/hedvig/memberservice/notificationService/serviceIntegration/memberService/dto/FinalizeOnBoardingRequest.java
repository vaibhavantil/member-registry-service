package com.hedvig.memberservice.notificationService.serviceIntegration.memberService.dto;

import lombok.Data;

@Data
public class FinalizeOnBoardingRequest {

    private String memberId;

    private String ssn;
    private String firstName;
    private String lastName;
    private String email;

    private Address address;

}
