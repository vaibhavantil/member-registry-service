package com.hedvig.external.bankID.bankIdRest;

import com.hedvig.external.bankID.bankIdRestTypes.CollectRequest;
import com.hedvig.external.bankID.bankIdRestTypes.CollectResponse;
import com.hedvig.external.bankID.bankIdRestTypes.OrderAuthRequest;
import com.hedvig.external.bankID.bankIdRestTypes.OrderResponse;
import com.hedvig.external.bankID.bankIdRestTypes.OrderSignRequest;
import com.hedvig.external.bankID.configuration.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(
    name = "bankId",
    url = "${hedvig.external.bankid.rest.baseurl:https://appapi2.bankid.com/rp/v5}",
    configuration = FeignConfiguration.class)
public interface BankIdRestClient {

  @RequestMapping(value = "/auth", method = RequestMethod.POST, produces = "application/json",
      consumes = "application/json")
  ResponseEntity<OrderResponse> auth(@RequestBody OrderAuthRequest request);

  @RequestMapping(value = "/sign", method = RequestMethod.POST, produces = "application/json",
      consumes = "application/json")
  ResponseEntity<OrderResponse> sign(@RequestBody OrderSignRequest request);

  @RequestMapping(value = "/collect", method = RequestMethod.POST, produces = "application/json",
  consumes = "application/json")
  ResponseEntity<CollectResponse> collect(@RequestBody CollectRequest request);
}
