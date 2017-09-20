package com.hedvig.generic.mustrename.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import lombok.Value;

import java.time.LocalDate;

@Value
public class BankIdAuthRequest {

    private String personalIdentityNumber;
    private boolean autoCreate = false;

}
