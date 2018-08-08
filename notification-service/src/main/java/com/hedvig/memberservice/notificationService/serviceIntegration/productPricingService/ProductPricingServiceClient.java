package com.hedvig.memberservice.notificationService.serviceIntegration.productPricingService;

import com.hedvig.memberservice.notificationService.serviceIntegration.productPricingService.dto.InsuranceNotificationDTO;
import java.time.LocalDate;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name= "productPricing", url = "${hedvig.productsPricing.url}")
public interface ProductPricingServiceClient {
  @GetMapping("/_/insurance/searchByActivationDate?activationDate={date}")
  ResponseEntity<List<InsuranceNotificationDTO>> getInsurancesByActivationDate(
      @PathVariable("date") LocalDate activationDate);
}
