package com.hedvig.external.bankID.bankidTypes;

import lombok.Value;

@Value
public class CollectResponse {
    protected ProgressStatus progressStatus;
    protected String signature;
    protected UserInfo userInfo;
    protected String ocspResponse;
}
