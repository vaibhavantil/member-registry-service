package com.hedvig.memberservice.notificationService.serviceIntegration.botService.messages.dto;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class BackOfficeResponseDTO {

    public String memberId;
    public String userId;

    public String msg;

    public BackOfficeResponseDTO(String memberId, String msg) {
        this.memberId = memberId;
        this.userId = memberId;
        this.msg = msg;
    }

}
