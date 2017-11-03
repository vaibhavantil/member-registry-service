package com.hedvig.memberservice.externalApi.prouctsPricing;

import com.hedvig.memberservice.externalApi.prouctsPricing.dto.ContractSignedRequest;
import com.hedvig.memberservice.externalApi.prouctsPricing.dto.SafetyIncreasersDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class ProductApi {

    private final ProductClient client;

    @Autowired
    public ProductApi(ProductClient client) {
        this.client = client;
    }

    public void contractSinged(long memberId, String referenceToken) {
        this.client.contractSinged(new ContractSignedRequest(Objects.toString(memberId), referenceToken));
    }

    public List<String> getSafetyIncreasers(long memberId) {
        try {
            ResponseEntity<SafetyIncreasersDTO> response = this.client.getSafetyIncreasers(memberId);
            return response.getBody().getItems();
        }catch (RestClientResponseException ex) {
            log.error("Error from products-pricing", ex);
        }
        return new ArrayList<>();
    }
}
