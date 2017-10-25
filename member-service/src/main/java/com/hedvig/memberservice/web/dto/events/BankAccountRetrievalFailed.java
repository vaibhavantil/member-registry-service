package com.hedvig.memberservice.web.dto.events;

public class BankAccountRetrievalFailed extends MemberServiceEventPayload {
    public String errorMsg;

    public BankAccountRetrievalFailed(String errorMsg) {

        this.errorMsg = errorMsg;
    }
}
