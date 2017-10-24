package com.hedvig.external.billectaAPI;

import com.hedvig.external.billectaAPI.api.*;
import feign.Headers;
import feign.RequestLine;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;


@Headers("Accept: application/xml")
@FeignClient(name = "billecta", url ="${hedvig.billecta.url}")
public interface BillectaClient {

    @RequestMapping(value = "/v1/creditors/creditors", method = RequestMethod.GET, produces = "application/xml")
    Creditors getAllCreditors(@RequestHeader("Authorization") String token);

    @RequestMapping(value = "/v1/debtors/debtor", method = RequestMethod.POST, produces = "application/xml")
    Created createDebtor(@RequestHeader("Authorization") String token, @RequestBody String debtor);


    @RequestMapping(value = "/v1/bank/accounts/{id}", method =  RequestMethod.POST, produces = "application/xml")
    Created initiateBankAccountRetrieval(
            @RequestBody MultiValueMap<String, String> m,
            @RequestHeader("Authorization") String token,
            @PathVariable("id") String id,
            @RequestParam("bank") String bank,
            @RequestParam("ssn") String ssn
            );

    @RequestMapping(value = "/v1/bank/accounts/{id}", method = RequestMethod.GET, produces = "application/xml")
    ResponseEntity<BankAccountRequest> getBankAccountNumbers(@RequestHeader("Authorization") String token, @PathVariable("id") String publicId);


}
