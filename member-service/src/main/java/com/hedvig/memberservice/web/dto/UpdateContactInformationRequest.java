package com.hedvig.memberservice.web.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateContactInformationRequest {

    private String memberId;
    private String firstName;
    private String lastName;
    private String email;

    private Address address;

}
