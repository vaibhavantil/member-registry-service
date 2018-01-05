package com.hedvig.external.bankID.bankidTypes;

import lombok.Value;

@Value
public class OrderResponse {
    protected String orderRef;
    protected String autoStartToken;
}
