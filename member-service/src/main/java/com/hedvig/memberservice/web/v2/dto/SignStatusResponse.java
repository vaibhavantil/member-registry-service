package com.hedvig.memberservice.web.v2.dto;

import com.hedvig.external.authentication.dto.ZignSecBankIdProgressStatus;
import com.hedvig.memberservice.entities.SignSession;
import com.hedvig.memberservice.entities.SignStatus;

import javax.validation.constraints.NotNull;

import com.hedvig.memberservice.services.signing.simple.dto.SimpleSignStatus;
import lombok.Value;
import lombok.val;

@Value
public class SignStatusResponse {

    @NotNull
    SignStatus status;

    CollectData collectData;

    public static SignStatusResponse CreateFromEntity(
        SignSession session) {

        val collectResponse = session.getCollectResponse();
        val data =
            collectResponse == null
                ? null
                : new CollectData(collectResponse.getStatus(), collectResponse.getHintCode());

        return new SignStatusResponse(session.getSignAndContractStatus(), data);
    }

    public static SignStatusResponse CreateFromZignSecStatus(@NotNull ZignSecBankIdProgressStatus status) {
        switch (status) {
            case INITIATED:
                return new SignStatusResponse(SignStatus.INITIATED, null);
            case IN_PROGRESS:
                return new SignStatusResponse(SignStatus.IN_PROGRESS, null);
            case FAILED:
                return new SignStatusResponse(SignStatus.FAILED, null);
            case COMPLETED:
                return new SignStatusResponse(SignStatus.COMPLETED, null);
        }

        throw new RuntimeException("Could not return SignStatusResponse from ZignSecBankIdProgressStatus: " + status + ".");
    }

    public static SignStatusResponse CreateFromSimpleSignStatus(@NotNull SimpleSignStatus status) {
        switch (status) {
            case INITIATED:
                return new SignStatusResponse(SignStatus.INITIATED, null);
            case CONTRACTS_CREATED:
                return new SignStatusResponse(SignStatus.COMPLETED, null);
        }

        throw new RuntimeException("Could not return SignStatusResponse from ZignSecBankIdProgressStatus: " + status + ".");
    }
}
