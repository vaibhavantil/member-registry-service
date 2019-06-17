package com.hedvig.memberservice.externalApi.productsPricing;

import com.hedvig.memberservice.externalApi.productsPricing.dto.*;
import feign.FeignException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductApi {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProductApi.class);
  private final ProductClient client;

  @Autowired
  public ProductApi(ProductClient client) {
    this.client = client;
  }

  public void memberCreated(
    long memberId
  ){
    this.client.memberCreated(
      new MemberCreatedRequest(
        Objects.toString(memberId)));
  }

  public void contractSinged(
      long memberId,
      String referenceToken,
      String signature,
      String oscpResponse,
      Instant signedOn,
      String ssn) {
    this.client.contractSinged(
        new ContractSignedRequest(
            Objects.toString(memberId), referenceToken, signature, oscpResponse, signedOn, ssn));
  }

  public void memberNameUpdate(
    long memberId,
    String name
  ) {
    this.client.memberNameUpdate(
      new MemberNameUpdateRequest(
        Objects.toString(memberId), name));
  }


  public List<String> getSafetyIncreasers(long memberId) {
    try {
      ResponseEntity<SafetyIncreasersDTO> response = this.client.getSafetyIncreasers(memberId);
      return response.getBody().getItems();
    } catch (FeignException ex) {
      if (ex.status() != 404) {
        log.error("Error from products-pricing", ex);
      }
    }
    return new ArrayList<>();
  }

  public InsuranceStatusDTO getInsuranceStatus(long memberId) {
    try {
      ResponseEntity<InsuranceStatusDTO> response = this.client.getInsuranceStatus(memberId);
      return response.getBody();
    } catch (FeignException ex) {
      if (ex.status() != 404) {
        log.error("Error getting insurance status from products-pricing", ex);
      }
    }

    return new InsuranceStatusDTO(new ArrayList<>(), "PENDING");
  }

  public byte[] getContract(String memberId) {
    ResponseEntity<byte[]> response = this.client.getContract(memberId);
    return response.getBody();
  }

  public void memberCancelledInsurance(Long memberId, UUID insuranceId, Instant inactivationDate) {
    SetCancellationDateRequest setCancellationDateRequest =
        new SetCancellationDateRequest(insuranceId, inactivationDate);
    ResponseEntity<String> responseEntity =
        this.client.setCancellationDate(memberId, setCancellationDateRequest);
    if (responseEntity.getStatusCode() != HttpStatus.ACCEPTED) {
      String message =
          String.format(
              "Got error response (%s) from product-pricing with body: %s",
              responseEntity.getStatusCode(), responseEntity.getBody());
      throw new RuntimeException(message);
    }
  }

  @SuppressWarnings("Duplicates")
  public List<InsuranceNotificationDTO> getInsurancesByActivationDate(LocalDate activationDate) {
    try {
      ResponseEntity<List<InsuranceNotificationDTO>> response =
          this.client.getInsurancesByActivationDate(
              activationDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
      return response.getBody();
    } catch (FeignException ex) {
      if (ex.status() != 404) {
        log.error("Error getting insurances by activationDate from products-pricing", ex);
      }
    }
    return new ArrayList<>();
  }

  public ProductToSignStatusDTO hasProductToSign(long memberId) {
    try {
      ResponseEntity<ProductToSignStatusDTO> response = this.client
          .hasProductToSign(String.valueOf(memberId));
      return Objects.requireNonNull(response.getBody());
    } catch (FeignException ex) {
      if (ex.status() == 404) {
        return new ProductToSignStatusDTO(false, false);
      } else {
        log.error("Error getting insurance from products-pricing {}", ex);
      }
    }
    return new ProductToSignStatusDTO(false, false);
  }
}
