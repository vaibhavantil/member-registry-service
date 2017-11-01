package com.hedvig.memberservice.externalApi.prouctsPricing;

import com.hedvig.memberservice.externalApi.prouctsPricing.dto.ContractSingedRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ProductApi {

    private final ProductClient client;

    @Autowired
    public ProductApi(ProductClient client) {
        this.client = client;
    }

    public void contractSinged(long memberId, String referenceToken) {
        this.client.contractSinged(new ContractSingedRequest(Objects.toString(memberId), referenceToken));
    }
}
