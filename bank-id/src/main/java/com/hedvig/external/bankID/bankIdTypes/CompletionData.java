package com.hedvig.external.bankID.bankIdTypes;

import com.hedvig.external.bankID.bankIdTypes.Collect.Cert;
import com.hedvig.external.bankID.bankIdTypes.Collect.Device;
import com.hedvig.external.bankID.bankIdTypes.Collect.User;
import lombok.Value;

@Value
public class CompletionData {
  private User user;
  private Device device;
  private Cert cert;
  private String signature;
  private String ocspResponse;
}
