package com.hedvig.memberservice.web.dto;

import com.hedvig.memberservice.query.MemberEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class InternalMember {
    private Long memberId;

    private String status;

    private String ssn;

    private String firstName;

    private String lastName;

    private String street;

    private Integer floor;

    private String apartment;

    private String city;

    private String zipCode;

    private String country;

    private String email;

    private String phoneNumber;

    private LocalDate birthDate;

    public static InternalMember fromEnity(MemberEntity entity) {
        val dto = new InternalMember();
        dto.setMemberId(entity.getId());
        dto.setStatus(entity.getStatus());
        dto.setSsn(entity.getSsn());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setStreet(entity.getStreet());
        dto.setFloor(entity.getFloor());
        dto.setApartment(entity.getApartment());
        dto.setCity(entity.getCity());
        dto.setZipCode(entity.getZipCode());
        dto.setCountry("SE");
        dto.setEmail(entity.getEmail());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setBirthDate(entity.getBirthDate());
        return dto;
    }
}
