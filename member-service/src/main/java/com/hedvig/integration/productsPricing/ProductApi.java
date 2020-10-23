package com.hedvig.integration.productsPricing;

import com.hedvig.integration.productsPricing.dto.*;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
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
    this.client.createdCampaignMember(
      new MemberCreatedRequest(
        Objects.toString(memberId)));
  }

  public void memberNameUpdate(
    long memberId,
    String name
  ) {
    this.client.updateCampaignMemberName(
      new MemberNameUpdateRequest(
        Objects.toString(memberId), name));
  }
}
