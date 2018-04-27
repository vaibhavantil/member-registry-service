package com.hedvig.memberservice.notificationService.serviceIntegration.memberService.dto;

import lombok.Data;

@Data
public class Address {
    private String street;
    private String city;
    private String zipCode;
    private String apartmentNo;
    private Integer floor;
}
