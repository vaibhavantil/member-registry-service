package com.hedvig.memberservice.web.dto;

import lombok.Data;

@Data
public class UpdateContactInformationRequest {

    private String memberId;
    private String firstName;
    private String lastName;
    private String email;

    private Address address;

}
