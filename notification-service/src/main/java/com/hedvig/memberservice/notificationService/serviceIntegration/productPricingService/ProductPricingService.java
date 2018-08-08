package com.hedvig.memberservice.notificationService.serviceIntegration.productPricingService;

import com.hedvig.memberservice.notificationService.serviceIntegration.productPricingService.dto.InsuranceNotificationDTO;
import feign.FeignException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

public class ProductPricingService {

  private final ProductPricingServiceClient productPricingServiceClient;
  private static final Logger log = LoggerFactory.getLogger(ProductPricingService.class);

  public ProductPricingService(ProductPricingServiceClient productPricingServiceClient) {
    this.productPricingServiceClient = productPricingServiceClient;
  }

  public List<InsuranceNotificationDTO> getInsurancesByActivationDate(LocalDate activationDate) {
    try {
      ResponseEntity<List<InsuranceNotificationDTO>> response =
          this.productPricingServiceClient.getInsurancesByActivationDate(activationDate);
      return response.getBody();
    } catch (FeignException ex) {
      if (ex.status() != 404) {
        log.error("Error getting insurances by activationDate from products-pricing", ex);
      }
    }
    return new ArrayList<>();
  }
}
