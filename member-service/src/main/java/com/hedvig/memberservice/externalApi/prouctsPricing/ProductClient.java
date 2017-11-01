package com.hedvig.memberservice.externalApi.prouctsPricing;

import com.hedvig.memberservice.externalApi.prouctsPricing.dto.ContractSignedRequest;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "productPricing", url ="${hedvig.productsPricing.url}")
public interface ProductClient {

    @RequestMapping(value = "/i/insurance/contractSigned", method = RequestMethod.POST)
    String contractSinged(@RequestBody ContractSignedRequest req);

}
