package com.hedvig.external.bankID.bankIdRestTypes;

import lombok.Value;

@Value
public class OrderResponse {
    protected String orderRef;
    protected String autoStartToken;
}
