package com.hedvig.memberservice.externalApi.productsPricing;

import com.hedvig.memberservice.externalApi.productsPricing.dto.ContractSignedRequest;
import com.hedvig.memberservice.externalApi.productsPricing.dto.InsuranceStatusDTO;
import com.hedvig.memberservice.externalApi.productsPricing.dto.SafetyIncreasersDTO;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class ProductApi {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProductApi.class);
    private final ProductClient client;

    @Autowired
    public ProductApi(ProductClient client) {
        this.client = client;
    }

    public void contractSinged(long memberId, String referenceToken, String signature, String oscpResponse) {
        this.client.contractSinged(new ContractSignedRequest(Objects.toString(memberId), referenceToken, signature, oscpResponse));
    }

    public List<String> getSafetyIncreasers(long memberId) {
        try {
            ResponseEntity<SafetyIncreasersDTO> response = this.client.getSafetyIncreasers(memberId);
            return response.getBody().getItems();
        }catch (FeignException ex) {
            if(ex.status() != 404) {
                log.error("Error from products-pricing", ex);
            }
        }
        return new ArrayList<>();
    }

    public InsuranceStatusDTO getInsuranceStatus(long memberId) {
        try{
            ResponseEntity<InsuranceStatusDTO> response = this.client.getInsuranceStatus(memberId);
            return response.getBody();
        }catch (FeignException ex) {
            if(ex.status() != 404) {
                log.error("Error getting insurance status from products-pricing", ex);
            }
        }

        return new InsuranceStatusDTO(new ArrayList<>(), "PENDING");
    }

    public byte[] getContract(String memberId) {
        ResponseEntity<byte[]> response = this.client.getContract(memberId);
        return response.getBody();
    }
}
