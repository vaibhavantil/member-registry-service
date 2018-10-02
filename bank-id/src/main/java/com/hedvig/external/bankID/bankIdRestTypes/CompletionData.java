package com.hedvig.external.bankID.bankIdRestTypes;

import com.hedvig.external.bankID.bankIdRestTypes.Collect.Cert;
import com.hedvig.external.bankID.bankIdRestTypes.Collect.Device;
import com.hedvig.external.bankID.bankIdRestTypes.Collect.User;
import lombok.Value;

@Value
public class CompletionData {
  private User user;
  private Device device;
  private Cert cert;
  private String signature;
  private String ocspResponse;
}
