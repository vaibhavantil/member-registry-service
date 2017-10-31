package com.hedvig.memberservice.web.dto.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({

        @JsonSubTypes.Type(value = BankAccountRetrievalSuccess.class, name = "bank_account_success"),
        @JsonSubTypes.Type(value = BankAccountRetrievalFailed.class,  name = "bank_account_failed"),
        @JsonSubTypes.Type(value = MemberSigned.class,  name = "member_signed")

})
public class MemberServiceEventPayload {

}
