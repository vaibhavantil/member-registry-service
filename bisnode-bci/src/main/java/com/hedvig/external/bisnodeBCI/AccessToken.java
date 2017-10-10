package com.hedvig.external.bisnodeBCI;

import lombok.Value;

@Value
public class AccessToken {

    private String access_token;
    private String token_type;
    private Integer expires_in;

}
