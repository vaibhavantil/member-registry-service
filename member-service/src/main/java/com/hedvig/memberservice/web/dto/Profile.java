package com.hedvig.memberservice.web.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.List;




@Value
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class Profile {

    private String name;
    private String firstName;
    private String lastName;
    private List<String> familyMembers;
    private Integer age;
    private String email;
    private String address;
    private Integer livingAreaSqm;
    private String maskedBankAccountNumber;
    private String selectedCashback;


}
